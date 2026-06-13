package com.fabricio.collabboard.ui.feed

import android.app.AlertDialog
import android.os.Bundle
import android.text.*
import android.view.*
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.fabricio.collabboard.R
import com.fabricio.collabboard.model.Project
import com.fabricio.collabboard.utils.Validators
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class FeedFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val allProjects = mutableListOf<Project>()
    private val filtered = mutableListOf<Project>()
    private lateinit var adapter: ProjectAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_feed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val rv = view.findViewById<RecyclerView>(R.id.rvProjects)
        adapter = ProjectAdapter(filtered) { project ->
            findNavController().navigate(R.id.action_feed_to_detail, bundleOf("projectId" to project.projectId))
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        view.findViewById<Button>(R.id.btnProfile).setOnClickListener {
            findNavController().navigate(R.id.action_feed_to_profile)
        }
        view.findViewById<Button>(R.id.btnNewProject).setOnClickListener { showNewProjectDialog() }
        view.findViewById<Button>(R.id.btnNotifications).setOnClickListener {
            findNavController().navigate(R.id.action_feed_to_notifications)
        }

        view.findViewById<EditText>(R.id.etSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) { filterProjects(s?.toString() ?: "") }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadProjects(view)
        listenUnreadCount(view)
    }

    private fun loadProjects(view: View) {
        val progress = view.findViewById<ProgressBar>(R.id.progressFeed)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)
        progress.visibility = View.VISIBLE
        db.collection("projects").orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                progress.visibility = View.GONE
                allProjects.clear()
                snap?.documents?.forEach { doc -> doc.toObject(Project::class.java)?.let { allProjects.add(it) } }
                filterProjects(view.findViewById<EditText>(R.id.etSearch).text.toString())
                tvEmpty.visibility = if (allProjects.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun listenUnreadCount(view: View) {
        val tvBadge = view.findViewById<TextView>(R.id.tvNotifBadge)
        val uid = auth.currentUser?.uid ?: return
        db.collection("notifications")
            .whereEqualTo("recipientId", uid)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snap, _ ->
                val count = snap?.size() ?: 0
                if (count > 0) { tvBadge.visibility = View.VISIBLE; tvBadge.text = count.toString() }
                else tvBadge.visibility = View.GONE
            }
    }

    private fun filterProjects(query: String) {
        filtered.clear()
        if (query.isBlank()) filtered.addAll(allProjects)
        else { val q = query.lowercase(); filtered.addAll(allProjects.filter { it.title.lowercase().contains(q) || it.techStack.lowercase().contains(q) || it.ownerName.lowercase().contains(q) }) }
        adapter.notifyDataSetChanged()
    }

    private fun showNewProjectDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_new_project, null)
        AlertDialog.Builder(requireContext()).setTitle("Post a Project").setView(dialogView)
            .setPositiveButton("Post") { _, _ ->
                val title = dialogView.findViewById<EditText>(R.id.etProjectTitle).text.toString().trim()
                val description = dialogView.findViewById<EditText>(R.id.etProjectDescription).text.toString().trim()
                val techStack = dialogView.findViewById<EditText>(R.id.etProjectTechStack).text.toString().trim()
                if (!Validators.isValidProjectTitle(title)) { Toast.makeText(requireContext(), "Title min 3 chars", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                if (!Validators.isValidTechStack(techStack)) { Toast.makeText(requireContext(), "Enter tech stack", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                postProject(title, description, techStack)
            }.setNegativeButton("Cancel", null).show()
    }

    private fun postProject(title: String, description: String, techStack: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
            val ownerName = userDoc.getString("displayName") ?: "Unknown"
            val projectId = db.collection("projects").document().id
            val project = Project(projectId = projectId, ownerId = uid, ownerName = ownerName, title = title, description = description, techStack = techStack)
            db.collection("projects").document(projectId).set(project)
                .addOnSuccessListener { Toast.makeText(requireContext(), "Posted! 🚀", Toast.LENGTH_SHORT).show() }
        }
    }
}
