package com.fabricio.collabboard.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.fabricio.collabboard.R
import com.fabricio.collabboard.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val notifications = mutableListOf<Notification>()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_notifications, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db   = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val rv      = view.findViewById<RecyclerView>(R.id.rvNotifications)
        val tvEmpty = view.findViewById<TextView>(R.id.tvNotificationsEmpty)
        val progress = view.findViewById<ProgressBar>(R.id.progressNotifications)

        view.findViewById<Button>(R.id.btnNotificationsBack).setOnClickListener {
            findNavController().navigateUp()
        }

        adapter = NotificationAdapter(notifications) { notif ->
            if (!notif.isRead) {
                db.collection("notifications").document(notif.notificationId)
                    .update("isRead", true)
            }
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val uid = auth.currentUser?.uid ?: return
        progress.visibility = View.VISIBLE
        tvEmpty.visibility  = View.GONE

        // No orderBy → no composite index needed; sort client-side
        db.collection("notifications")
            .whereEqualTo("recipientId", uid)
            .addSnapshotListener { snap, error ->
                progress.visibility = View.GONE
                if (error != null) {
                    Log.e("NotificationsFragment", "Firestore error: ${error.message}")
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                notifications.clear()
                snap?.documents?.forEach { doc ->
                    doc.toObject(Notification::class.java)?.let { notifications.add(it) }
                }
                // Sort: unread first, then newest
                notifications.sortWith(compareByDescending<Notification> { !it.isRead }
                    .thenByDescending { it.createdAt.seconds })
                adapter.notifyDataSetChanged()
                tvEmpty.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE
            }
    }
}
