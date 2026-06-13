package com.fabricio.collabboard.ui.requests

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.fabricio.collabboard.R
import com.fabricio.collabboard.model.JoinRequest
import com.fabricio.collabboard.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RequestsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val requests = mutableListOf<JoinRequest>()
    private lateinit var adapter: RequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_requests, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val projectId = arguments?.getString("projectId") ?: return
        val projectTitle = arguments?.getString("projectTitle") ?: "Project"

        view.findViewById<TextView>(R.id.tvRequestsTitle).text = "Applicants – $projectTitle"
        view.findViewById<Button>(R.id.btnRequestsBack).setOnClickListener {
            findNavController().navigateUp()
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvRequests)
        val tvEmpty = view.findViewById<TextView>(R.id.tvRequestsEmpty)
        val progress = view.findViewById<ProgressBar>(R.id.progressRequests)

        adapter = RequestAdapter(
            items = requests,
            onAccept = { req -> handleRequest(req, "accepted", projectTitle) },
            onReject  = { req -> handleRequest(req, "rejected", projectTitle) }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        progress.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        // No orderBy → avoids needing a composite Firestore index
        db.collection("requests")
            .whereEqualTo("projectId", projectId)
            .addSnapshotListener { snap, error ->
                progress.visibility = View.GONE
                if (error != null) {
                    Log.e("RequestsFragment", "Firestore error: ${error.message}")
                    Toast.makeText(requireContext(), "Error loading requests: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                requests.clear()
                snap?.documents?.forEach { doc ->
                    doc.toObject(JoinRequest::class.java)?.let { requests.add(it) }
                }
                // Sort client-side: pending first, then by name
                requests.sortWith(compareBy(
                    { if (it.status == "pending") 0 else 1 },
                    { it.applicantName }
                ))
                adapter.notifyDataSetChanged()
                tvEmpty.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun handleRequest(req: JoinRequest, newStatus: String, projectTitle: String) {
        val label = if (newStatus == "accepted") "Accept" else "Reject"
        AlertDialog.Builder(requireContext())
            .setTitle("$label Request")
            .setMessage("$label ${req.applicantName}'s request?")
            .setPositiveButton(label) { _, _ ->
                db.collection("requests").document(req.requestId)
                    .update("status", newStatus)
                    .addOnSuccessListener {
                        sendNotificationToApplicant(req, newStatus, projectTitle)
                        Toast.makeText(requireContext(), "${req.applicantName} $newStatus", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendNotificationToApplicant(req: JoinRequest, status: String, projectTitle: String) {
        val emoji = if (status == "accepted") "✅" else "❌"
        val verb  = if (status == "accepted") "accepted! Welcome aboard 🎉" else "declined."
        val type  = if (status == "accepted") "request_accepted" else "request_rejected"
        // Deterministic ID so Cloud Function won't create a duplicate
        val notifId = "${status}_notif_${req.projectId}_${req.applicantId}"
        val notif = Notification(
            notificationId = notifId,
            recipientId    = req.applicantId,
            message        = "$emoji Your request to join \"$projectTitle\" was $verb",
            projectId      = req.projectId,
            projectTitle   = projectTitle,
            applicantId    = req.applicantId,
            applicantName  = req.applicantName,
            type           = type,
            isRead         = false
        )
        db.collection("notifications").document(notifId).set(notif)
    }
}
