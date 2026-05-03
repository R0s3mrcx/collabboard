package com.fabricio.collabboard.ui.detail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fabricio.collabboard.R
import com.fabricio.collabboard.model.JoinRequest
import com.fabricio.collabboard.model.Project
import com.fabricio.collabboard.utils.Validators
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val projectId = arguments?.getString("projectId") ?: return
        val progressBar = view.findViewById<ProgressBar>(R.id.progressDetail)
        progressBar.visibility = View.VISIBLE

        db.collection("projects").document(projectId).get()
            .addOnSuccessListener { doc ->
                progressBar.visibility = View.GONE
                val project = doc.toObject(Project::class.java) ?: return@addOnSuccessListener
                bindProject(view, project)
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error loading project", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bindProject(view: View, project: Project) {
        view.findViewById<TextView>(R.id.tvDetailTitle).text = project.title
        view.findViewById<TextView>(R.id.tvDetailDescription).text = project.description
        view.findViewById<TextView>(R.id.tvDetailTechStack).text = project.techStack
        view.findViewById<TextView>(R.id.tvDetailOwner).text = project.ownerName

        val tvStatus = view.findViewById<TextView>(R.id.tvDetailStatus)
        tvStatus.text = if (project.status == "open") "🟢 Open" else "🔴 Closed"

        val btnJoin = view.findViewById<Button>(R.id.btnJoin)
        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)
        val btnToggleStatus = view.findViewById<Button>(R.id.btnToggleStatus)

        val isOwner = project.ownerId == auth.currentUser?.uid

        if (isOwner) {
            btnJoin.text = "This is your project"
            btnJoin.isEnabled = false
            btnEdit.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE
            btnToggleStatus.visibility = View.VISIBLE
            btnToggleStatus.text = if (project.status == "open") "Close Project" else "Reopen Project"

            btnEdit.setOnClickListener { showEditDialog(project) }
            btnDelete.setOnClickListener { showDeleteConfirmation(project) }
            btnToggleStatus.setOnClickListener { toggleStatus(project) }
        } else {
            btnEdit.visibility = View.GONE
            btnDelete.visibility = View.GONE
            btnToggleStatus.visibility = View.GONE

            if (project.status == "closed") {
                btnJoin.text = "Project is Closed"
                btnJoin.isEnabled = false
            } else {
                btnJoin.setOnClickListener { sendJoinRequest(project) }
            }
        }
    }

    private fun showEditDialog(project: Project) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_new_project, null)

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
                    Toast.makeText(requireContext(), "Title must be at least 3 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (!Validators.isValidTechStack(techStack)) {
                    Toast.makeText(requireContext(), "Please enter the tech stack", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                db.collection("projects").document(project.projectId)
                    .update(mapOf("title" to title, "description" to description, "techStack" to techStack))
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Project updated ✅", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(project: Project) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Project")
            .setMessage("Are you sure you want to delete \"${project.title}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("projects").document(project.projectId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Project deleted", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Delete failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleStatus(project: Project) {
        val newStatus = if (project.status == "open") "closed" else "open"
        db.collection("projects").document(project.projectId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Status changed to $newStatus", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
    }

    private fun sendJoinRequest(project: Project) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val applicantName = userDoc.getString("displayName") ?: "Unknown"
                val requestId = db.collection("requests").document().id
                val request = JoinRequest(
                    requestId = requestId,
                    projectId = project.projectId,
                    applicantId = uid,
                    applicantName = applicantName,
                    message = "$applicantName wants to join your project"
                )
                db.collection("requests").document(requestId).set(request)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Request sent! 🚀", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}
