package com.fabricio.collabboard.ui.notifications

import android.graphics.Color
import android.graphics.Typeface
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.*
import com.fabricio.collabboard.R
import com.fabricio.collabboard.model.Notification

class NotificationAdapter(
    private val items: List<Notification>,
    private val onMarkRead: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvNotifMessage)
        val tvTime: TextView = view.findViewById(R.id.tvNotifTime)
        val tvUnread: TextView = view.findViewById(R.id.tvNotifUnread)
        val tvType: TextView = view.findViewById(R.id.tvNotifType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val notif = items[position]
        holder.tvMessage.text = notif.message
        holder.tvMessage.setTypeface(null, if (!notif.isRead) Typeface.BOLD else Typeface.NORMAL)
        holder.tvUnread.visibility = if (!notif.isRead) View.VISIBLE else View.GONE

        // Type label + color
        when (notif.type) {
            "join_request" -> {
                holder.tvType.text = "New Applicant"
                holder.tvType.setTextColor(Color.parseColor("#3949AB"))
            }
            "request_accepted" -> {
                holder.tvType.text = "Accepted ✅"
                holder.tvType.setTextColor(Color.parseColor("#2E7D32"))
            }
            "request_rejected" -> {
                holder.tvType.text = "Rejected ❌"
                holder.tvType.setTextColor(Color.parseColor("#C62828"))
            }
            "project_closed" -> {
                holder.tvType.text = "Project Closed"
                holder.tvType.setTextColor(Color.parseColor("#E65100"))
            }
            else -> {
                holder.tvType.text = "Notification"
                holder.tvType.setTextColor(Color.GRAY)
            }
        }

        val sdf = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault())
        holder.tvTime.text = sdf.format(notif.createdAt.toDate())
        holder.itemView.setOnClickListener { onMarkRead(notif) }

        // Background tint for unread
        holder.itemView.setBackgroundColor(
            if (!notif.isRead) Color.parseColor("#F0F4FF") else Color.TRANSPARENT
        )
    }

    override fun getItemCount() = items.size
}
