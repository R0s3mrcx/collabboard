package com.fabricio.collabboard.ui.feed

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fabricio.collabboard.R
import com.fabricio.collabboard.model.Project

class ProjectAdapter(
    private val projects: List<Project>,
    private val onClick: (Project) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvTechStack: TextView = view.findViewById(R.id.tvTechStack)
        val tvOwner: TextView = view.findViewById(R.id.tvOwner)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = projects[position]
        holder.tvTitle.text = project.title
        holder.tvDescription.text = project.description
        holder.tvTechStack.text = "🛠 ${project.techStack}"
        holder.tvOwner.text = "by ${project.ownerName}"

        if (project.status == "open") {
            holder.tvStatus.text = "🟢 Open"
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
        } else {
            holder.tvStatus.text = "🔴 Closed"
            holder.tvStatus.setTextColor(Color.parseColor("#C62828"))
        }

        holder.itemView.setOnClickListener { onClick(project) }
    }

    override fun getItemCount() = projects.size
}
