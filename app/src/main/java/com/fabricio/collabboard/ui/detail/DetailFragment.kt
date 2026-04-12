package com.fabricio.collabboard.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fabricio.collabboard.R
import com.fabricio.collabboard.model.JoinRequest
import com.fabricio.collabboard.model.Project
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

        db.collection("projects").document(projectId).get()
            .addOnSuccessListener { doc ->
                val project = doc.toObject(Project::class.java) ?: return@addOnSuccessListener

                view.findViewById<TextView>(R.id.tvDetailTitle).text = project.title
                view.findViewById<TextView>(R.id.tvDetailDescription).text = project.description
                view.findViewById<TextView>(R.id.tvDetailTechStack).text = project.techStack
                view.findViewById<TextView>(R.id.tvDetailOwner).text = project.ownerName

                val btnJoin = view.findViewById<Button>(R.id.btnJoin)

                if (project.ownerId == auth.currentUser?.uid) {
                    btnJoin.text = "This is your project"
                    btnJoin.isEnabled = false
                    return@addOnSuccessListener
                }

                btnJoin.setOnClickListener {
                    sendJoinRequest(project)
                }
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
                        Toast.makeText(requireContext(),
                            "Request sent! 🚀", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}

