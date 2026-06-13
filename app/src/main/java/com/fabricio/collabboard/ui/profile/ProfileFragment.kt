package com.fabricio.collabboard.ui.profile

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.fabricio.collabboard.R
import com.fabricio.collabboard.model.Project
import com.fabricio.collabboard.ui.feed.ProjectAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val myProjects = mutableListOf<Project>()
    private lateinit var adapter: ProjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db   = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val uid = auth.currentUser?.uid ?: return

        val rvMyProjects = view.findViewById<RecyclerView>(R.id.rvMyProjects)
        val tvEmpty      = view.findViewById<TextView>(R.id.tvProfileProjectsEmpty)
        val progress     = view.findViewById<ProgressBar>(R.id.progressProfile)

        // Projects are clickable → navigate to Detail (owner controls show automatically)
        adapter = ProjectAdapter(myProjects) { project ->
            findNavController().navigate(
                R.id.action_profile_to_detail,
                bundleOf("projectId" to project.projectId)
            )
        }
        rvMyProjects.layoutManager = LinearLayoutManager(requireContext())
        rvMyProjects.adapter = adapter

        // Load user info
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                view.findViewById<TextView>(R.id.tvProfileName).text =
                    doc.getString("displayName") ?: "User"
                view.findViewById<TextView>(R.id.tvProfileEmail).text =
                    doc.getString("email") ?: ""
                val skills = doc.getString("skills") ?: ""
                val tvSkills = view.findViewById<TextView>(R.id.tvProfileSkills)
                if (skills.isNotBlank()) {
                    tvSkills.text = "🛠 $skills"
                    tvSkills.visibility = View.VISIBLE
                } else {
                    tvSkills.visibility = View.GONE
                }
            }

        // Load my projects with real-time listener
        progress.visibility = View.VISIBLE
        db.collection("projects")
            .whereEqualTo("ownerId", uid)
            .addSnapshotListener { snap, error ->
                progress.visibility = View.GONE
                if (error != null) return@addSnapshotListener
                myProjects.clear()
                snap?.documents?.forEach { doc ->
                    doc.toObject(Project::class.java)?.let { myProjects.add(it) }
                }
                myProjects.sortByDescending { it.createdAt }
                adapter.notifyDataSetChanged()
                tvEmpty.visibility = if (myProjects.isEmpty()) View.VISIBLE else View.GONE
                rvMyProjects.visibility = if (myProjects.isEmpty()) View.GONE else View.VISIBLE
            }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_profile_to_login)
        }
    }
}
