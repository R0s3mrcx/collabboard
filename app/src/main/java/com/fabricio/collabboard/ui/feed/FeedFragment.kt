package com.fabricio.collabboard.ui.feed

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fabricio.collabboard.R
import com.fabricio.collabboard.model.Project
import com.fabricio.collabboard.utils.Validators
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FeedFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val projects = mutableListOf<Project>()
    private lateinit var adapter: ProjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_feed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val rvProjects = view.findViewById<RecyclerView>(R.id.rvProjects)
        adapter = ProjectAdapter(projects) { project ->
            findNavController().navigate(
                R.id.action_feed_to_detail,
                bundleOf("projectId" to project.projectId)
            )
        }
        rvProjects.layoutManager = LinearLayoutManager(requireContext())
        rvProjects.adapter = adapter

        view.findViewById<Button>(R.id.btnProfile).setOnClickListener {
            findNavController().navigate(R.id.action_feed_to_profile)
        }

        view.findViewById<Button>(R.id.btnNewProject).setOnClickListener {
            showNewProjectDialog()
        }

        loadProjects()
    }

    private fun loadProjects() {
        db.collection("projects")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    projects.clear()
                    for (doc in snapshot.documents) {
                        doc.toObject(Project::class.java)?.let { projects.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun showNewProjectDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_new_project, null)

        AlertDialog.Builder(requireContext())
            .setTitle("Post a Project")
            .setView(dialogView)
            .setPositiveButton("Post") { _, _ ->
                val title = dialogView.findViewById<EditText>(R.id.etProjectTitle)
                    .text.toString().trim()
                val description = dialogView.findViewById<EditText>(R.id.etProjectDescription)
                    .text.toString().trim()
                val techStack = dialogView.findViewById<EditText>(R.id.etProjectTechStack)
                    .text.toString().trim()

                if (!Validators.isValidProjectTitle(title)) {
                    Toast.makeText(requireContext(),
                        "Title must be at least 3 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (!Validators.isValidTechStack(techStack)) {
                    Toast.makeText(requireContext(),
                        "Please enter the tech stack needed", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                postProject(title, description, techStack)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun postProject(title: String, description: String, techStack: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val ownerName = userDoc.getString("displayName") ?: "Unknown"
                val projectId = db.collection("projects").document().id
                val project = Project(
                    projectId = projectId,
                    ownerId = uid,
                    ownerName = ownerName,
                    title = title,
                    description = description,
                    techStack = techStack
                )
                db.collection("projects").document(projectId).set(project)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(),
                            "Project posted! 🚀", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}