package com.fabricio.collabboard.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val uid = auth.currentUser?.uid ?: return

        val rvMyProjects = view.findViewById<RecyclerView>(R.id.rvMyProjects)
        adapter = ProjectAdapter(myProjects) {}
        rvMyProjects.layoutManager = LinearLayoutManager(requireContext())
        rvMyProjects.adapter = adapter

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                view.findViewById<TextView>(R.id.tvProfileName).text =
                    doc.getString("displayName") ?: "User"
                view.findViewById<TextView>(R.id.tvProfileEmail).text =
                    doc.getString("email") ?: ""
                view.findViewById<TextView>(R.id.tvProfileSkills).text =
                    "🛠 ${doc.getString("skills") ?: ""}"
            }

        db.collection("projects")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                myProjects.clear()
                for (doc in snapshot.documents) {
                    doc.toObject(Project::class.java)?.let { myProjects.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_profile_to_login)
        }
    }
}

