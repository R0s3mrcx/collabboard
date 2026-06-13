package com.fabricio.collabboard.ui.detail

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fabricio.collabboard.R
import com.fabricio.collabboard.model.JoinRequest
import com.fabricio.collabboard.model.Notification
import com.fabricio.collabboard.model.Project
import com.fabricio.collabboard.utils.Validators
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentProject: Project? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btnDetailBack).setOnClickListener {
            findNavController().navigateUp()
        }
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val projectId = arguments?.getString("projectId") ?: return
        val progress = view.findViewById<ProgressBar>(R.id.progressDetail)
        progress.visibility = View.VISIBLE

        // Real-time listener so status toggle refreshes instantly
        db.collection("projects").document(projectId)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null) {
                    progress.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error loading project", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                progress.visibility = View.GONE
                val project = doc.toObject(Project::class.java) ?: return@addSnapshotListener
                currentProject = project
                bindProject(view, project)
            }
    }

    private fun bindProject(view: View, project: Project) {
        view.findViewById<TextView>(R.id.tvDetailTitle).text = project.title
        view.findViewById<TextView>(R.id.tvDetailDescription).text = project.description
        view.findViewById<TextView>(R.id.tvDetailTechStack).text = project.techStack
        view.findViewById<TextView>(R.id.tvDetailOwner).text = project.ownerName
        view.findViewById<TextView>(R.id.tvDetailStatus).text =
            if (project.status == "open") "🟢 Open" else "🔴 Closed"

        val btnJoin = view.findViewById<Button>(R.id.btnJoin)
        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)
        val btnToggleStatus = view.findViewById<Button>(R.id.btnToggleStatus)
        val btnViewApplicants = view.findViewById<Button>(R.id.btnViewApplicants)
        val isOwner = project.ownerId == auth.currentUser?.uid

        if (isOwner) {
            btnJoin.visibility = View.GONE
            btnEdit.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE
            btnToggleStatus.visibility = View.VISIBLE
            btnViewApplicants.visibility = View.VISIBLE

            btnToggleStatus.text = if (project.status == "open") "🔒 Close Project" else "🔓 Reopen Project"

            // Live applicant count badge
            db.collection("requests")
                .whereEqualTo("projectId", project.projectId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener { snap, _ ->
                    val count = snap?.size() ?: 0
                    btnViewApplicants.text = if (count > 0)
                        "👥 View Applicants ($count pending)"
                    else
                        "👥 View Applicants"
                }

            btnViewApplicants.setOnClickListener {
                findNavController().navigate(
                    R.id.action_detail_to_requests,
                    bundleOf(
                        "projectId" to project.projectId,
                        "projectTitle" to project.title
                    )
                )
            }
            btnEdit.setOnClickListener { showEditDialog(project) }
            btnDelete.setOnClickListener { showDeleteConfirmation(project) }
            btnToggleStatus.setOnClickListener { toggleStatus(project) }
        } else {
            btnEdit.visibility = View.GONE
            btnDelete.visibility = View.GONE
            btnToggleStatus.visibility = View.GONE
            btnViewApplicants.visibility = View.GONE
            btnJoin.visibility = View.VISIBLE

            if (project.status == "closed") {
                btnJoin.text = "🔴 Project is Closed"
                btnJoin.isEnabled = false
            } else {
                btnJoin.text = "I'm In 🚀"
                btnJoin.isEnabled = true
                // Check if already applied
                val uid = auth.currentUser?.uid ?: return
                db.collection("requests")
                    .whereEqualTo("projectId", project.projectId)
                    .whereEqualTo("applicantId", uid)
                    .get()
                    .addOnSuccessListener { existing ->
                        if (!existing.isEmpty) {
                            btnJoin.text = "✅ Already Applied"
                            btnJoin.isEnabled = false
                        } else {
                            btnJoin.setOnClickListener { sendJoinRequest(project) }
                        }
                    }
            }
        }
    }

    private fun sendJoinRequest(project: Project) {
        val uid = auth.currentUser?.uid ?: return
        val btnJoin = view?.findViewById<Button>(R.id.btnJoin) ?: return
        btnJoin.isEnabled = false
        btnJoin.text = "Sending..."

        db.collection("requests")
            .whereEqualTo("projectId", project.projectId)
            .whereEqualTo("applicantId", uid)
            .get()
            .addOnSuccessListener { existing ->
                if (!existing.isEmpty) {
                    btnJoin.text = "✅ Already Applied"
                    return@addOnSuccessListener
                }
                db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
                    val applicantName = userDoc.getString("displayName") ?: "Unknown"
                    val requestId = db.collection("requests").document().id
                    val request = JoinRequest(
                        requestId = requestId,
                        projectId = project.projectId,
                        projectTitle = project.title,
                        applicantId = uid,
                        applicantName = applicantName,
                        message = "$applicantName wants to join your project"
                    )
                    db.collection("requests").document(requestId).set(request)
                        .addOnSuccessListener {
                            // Cloud Function handles owner notification automatically.
                            // We also write it here as fallback (idempotent via notificationId).
                            createOwnerNotification(project, uid, applicantName)
                            btnJoin.text = "✅ Request Sent!"
                            Toast.makeText(requireContext(), "Request sent! 🚀", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            btnJoin.isEnabled = true
                            btnJoin.text = "I'm In 🚀"
                            Toast.makeText(requireContext(), "Failed to send request", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun createOwnerNotification(project: Project, applicantId: String, applicantName: String) {
        // Use deterministic ID so Cloud Function duplicate doesn't matter
        val notifId = "req_notif_${project.projectId}_$applicantId"
        val notif = Notification(
            notificationId = notifId,
            recipientId = project.ownerId,
            message = "🔔 $applicantName wants to join \"${project.title}\"",
            projectId = project.projectId,
            projectTitle = project.title,
            applicantId = applicantId,
            applicantName = applicantName,
            type = "join_request",
            isRead = false
        )
        db.collection("notifications").document(notifId).set(notif)
    }

    private fun showEditDialog(project: Project) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_new_project, null)
        dialogView.findViewById<EditText>(R.id.etProjectTitle).setText(project.title)
        dialogView.findViewById<EditText>(R.id.etProjectDescription).setText(project.description)
        dialogView.findViewById<EditText>(R.id.etProjectTechStack).setText(project.techStack)
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Project")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = dialogView.findViewById<EditText>(R.id.etProjectTitle).text.toString().trim()
                val description = dialogView.findViewById<EditText>(R.id.etProjectDescription).text.toString().trim()
                val techStack = dialogView.findViewById<EditText>(R.id.etProjectTechStack).text.toString().trim()
                if (!Validators.isValidProjectTitle(title)) {
                    Toast.makeText(requireContext(), "Title min 3 chars", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (!Validators.isValidTechStack(techStack)) {
                    Toast.makeText(requireContext(), "Enter tech stack", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                db.collection("projects").document(project.projectId)
                    .update(mapOf("title" to title, "description" to description, "techStack" to techStack))
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Updated ✅", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(project: Project) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Project")
            .setMessage("Delete \"${project.title}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("projects").document(project.projectId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleStatus(project: Project) {
        val newStatus = if (project.status == "open") "closed" else "open"
        // Snapshot listener will auto-refresh the UI, no navigateUp needed
        db.collection("projects").document(project.projectId)
            .update("status", newStatus)
            .addOnSuccessListener {
                val msg = if (newStatus == "closed") "Project closed 🔒" else "Project reopened 🔓"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
    }
}
