package com.fabricio.collabboard.ui.requests

import android.graphics.Color
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.*
import com.fabricio.collabboard.R
import com.fabricio.collabboard.model.JoinRequest

class RequestAdapter(
    private val items: List<JoinRequest>,
    private val onAccept: (JoinRequest) -> Unit,
    private val onReject: (JoinRequest) -> Unit
) : RecyclerView.Adapter<RequestAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvReqApplicantName)
        val tvMessage: TextView = view.findViewById(R.id.tvReqMessage)
        val tvStatus: TextView = view.findViewById(R.id.tvReqStatus)
        val tvTime: TextView = view.findViewById(R.id.tvReqTime)
        val btnAccept: Button = view.findViewById(R.id.btnAccept)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val req = items[position]
        holder.tvName.text = req.applicantName
        holder.tvMessage.text = req.message.ifBlank { "${req.applicantName} wants to join your project" }

        val sdf = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault())
        holder.tvTime.text = sdf.format(req.createdAt.toDate())

        when (req.status) {
            "pending" -> {
                holder.tvStatus.text = "⏳ Pending"
                holder.tvStatus.setTextColor(Color.parseColor("#E65100"))
                holder.btnAccept.visibility = View.VISIBLE
                holder.btnReject.visibility = View.VISIBLE
            }
            "accepted" -> {
                holder.tvStatus.text = "✅ Accepted"
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
                holder.btnAccept.visibility = View.GONE
                holder.btnReject.visibility = View.GONE
            }
            "rejected" -> {
                holder.tvStatus.text = "❌ Rejected"
                holder.tvStatus.setTextColor(Color.parseColor("#C62828"))
                holder.btnAccept.visibility = View.GONE
                holder.btnReject.visibility = View.GONE
            }
        }

        holder.btnAccept.setOnClickListener { onAccept(req) }
        holder.btnReject.setOnClickListener { onReject(req) }
    }

    override fun getItemCount() = items.size
}
