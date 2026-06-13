package com.fabricio.collabboard.ui.feed

import android.graphics.Color
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.*
import com.fabricio.collabboard.R
import com.fabricio.collabboard.model.Project

class ProjectAdapter(
    private val projects: List<Project>,
    private val onClick: (Project) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvTechStack: TextView = view.findViewById(R.id.tvTechStack)
        val tvOwner: TextView = view.findViewById(R.id.tvOwner)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_project, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = projects[position]
        holder.tvTitle.text = p.title
        holder.tvDescription.text = p.description
        holder.tvTechStack.text = "🛠 ${p.techStack}"
        holder.tvOwner.text = "by ${p.ownerName}"
        if (p.status == "open") { holder.tvStatus.text = "🟢 Open"; holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")) }
        else { holder.tvStatus.text = "🔴 Closed"; holder.tvStatus.setTextColor(Color.parseColor("#C62828")) }
        holder.itemView.setOnClickListener { onClick(p) }
    }

    override fun getItemCount() = projects.size
}
