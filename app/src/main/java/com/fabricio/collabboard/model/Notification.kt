package com.fabricio.collabboard.model

import com.google.firebase.Timestamp

data class Notification(
    val notificationId: String = "",
    val recipientId: String = "",
    val message: String = "",
    val projectId: String = "",
    val projectTitle: String = "",
    val applicantName: String = "",
    val applicantId: String = "",
    val type: String = "join_request", // "join_request" | "project_closed" | "request_accepted" | "request_rejected"
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)
