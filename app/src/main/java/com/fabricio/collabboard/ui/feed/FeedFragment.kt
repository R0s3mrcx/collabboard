package com.fabricio.collabboard.ui.feed

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
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
    private val allProjects = mutableListOf<Project>()
    private val filteredProjects = mutableListOf<Project>()
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
        adapter = ProjectAdapter(filteredProjects) { project ->
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

        // Search/Filter (Extension 1)
        view.findViewById<EditText>(R.id.etSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProjects(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadProjects(view)
    }

    private fun loadProjects(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progressFeed)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)
        progressBar.visibility = View.VISIBLE

        db.collection("projects")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                progressBar.visibility = View.GONE
                if (snapshot != null) {
                    allProjects.clear()
                    for (doc in snapshot.documents) {
                        doc.toObject(Project::class.java)?.let { allProjects.add(it) }
                    }
                    val query = view.findViewById<EditText>(R.id.etSearch).text.toString()
                    filterProjects(query)

                    // Empty state (Extension 2)
                    tvEmpty.visibility = if (allProjects.isEmpty()) View.VISIBLE else View.GONE
                }
            }
    }

    private fun filterProjects(query: String) {
        filteredProjects.clear()
        if (query.isBlank()) {
            filteredProjects.addAll(allProjects)
        } else {
            val q = query.lowercase()
            filteredProjects.addAll(allProjects.filter {
                it.title.lowercase().contains(q) ||
                it.techStack.lowercase().contains(q) ||
                it.ownerName.lowercase().contains(q)
            })
        }
        adapter.notifyDataSetChanged()
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
                        Toast.makeText(requireContext(), "Project posted! 🚀", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}
