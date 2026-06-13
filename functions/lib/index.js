"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.onRequestStatusChange = exports.onProjectStatusChange = exports.onNewJoinRequest = void 0;
const app_1 = require("firebase-admin/app");
const firestore_1 = require("firebase-admin/firestore");
const firestore_2 = require("firebase-functions/v2/firestore");
(0, app_1.initializeApp)();
const db = (0, firestore_1.getFirestore)();
/**
 * Trigger: new join request created → notify the project owner.
 * Uses a deterministic notificationId so the Android fallback write is idempotent.
 */
exports.onNewJoinRequest = (0, firestore_2.onDocumentCreated)("requests/{requestId}", async (event) => {
    const request = event.data?.data();
    if (!request)
        return;
    const projectDoc = await db.collection("projects").doc(request.projectId).get();
    if (!projectDoc.exists)
        return;
    const project = projectDoc.data();
    const notifId = `req_notif_${request.projectId}_${request.applicantId}`;
    await db.collection("notifications").doc(notifId).set({
        notificationId: notifId,
        recipientId: project.ownerId,
        message: `🔔 ${request.applicantName} wants to join "${project.title}"`,
        projectId: request.projectId,
        projectTitle: project.title,
        applicantId: request.applicantId,
        applicantName: request.applicantName,
        type: "join_request",
        isRead: false,
        createdAt: firestore_1.FieldValue.serverTimestamp(),
    }, { merge: true });
    console.log(`[onNewJoinRequest] Notif ${notifId} → owner ${project.ownerId}`);
});
/**
 * Trigger: project status changes to "closed" → notify all pending applicants.
 */
exports.onProjectStatusChange = (0, firestore_2.onDocumentUpdated)("projects/{projectId}", async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after)
        return;
    if (before.status === after.status)
        return;
    if (after.status !== "closed")
        return;
    const projectId = event.params.projectId;
    const requestsSnap = await db
        .collection("requests")
        .where("projectId", "==", projectId)
        .where("status", "==", "pending")
        .get();
    if (requestsSnap.empty)
        return;
    const batch = db.batch();
    requestsSnap.docs.forEach((doc) => {
        const req = doc.data();
        const notifId = `closed_notif_${projectId}_${req.applicantId}`;
        batch.set(db.collection("notifications").doc(notifId), {
            notificationId: notifId,
            recipientId: req.applicantId,
            message: `ℹ️ "${after.title}" has been closed by the owner`,
            projectId: projectId,
            projectTitle: after.title,
            applicantId: req.applicantId,
            applicantName: req.applicantName,
            type: "project_closed",
            isRead: false,
            createdAt: firestore_1.FieldValue.serverTimestamp(),
        }, { merge: true });
    });
    await batch.commit();
    console.log(`[onProjectStatusChange] Closed notifs sent for project ${projectId}`);
});
/**
 * Trigger: request status changes accepted/rejected → notify the applicant.
 */
exports.onRequestStatusChange = (0, firestore_2.onDocumentUpdated)("requests/{requestId}", async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after)
        return;
    if (before.status === after.status)
        return;
    if (after.status !== "accepted" && after.status !== "rejected")
        return;
    const emoji = after.status === "accepted" ? "✅" : "❌";
    const verb = after.status === "accepted" ? "accepted! Welcome aboard 🎉" : "declined.";
    const type = after.status === "accepted" ? "request_accepted" : "request_rejected";
    const notifId = `${after.status}_notif_${after.projectId}_${after.applicantId}`;
    await db.collection("notifications").doc(notifId).set({
        notificationId: notifId,
        recipientId: after.applicantId,
        message: `${emoji} Your request to join "${after.projectTitle}" was ${verb}`,
        projectId: after.projectId,
        projectTitle: after.projectTitle,
        applicantId: after.applicantId,
        applicantName: after.applicantName,
        type: type,
        isRead: false,
        createdAt: firestore_1.FieldValue.serverTimestamp(),
    }, { merge: true });
    console.log(`[onRequestStatusChange] ${type} notif → ${after.applicantId}`);
});
//# sourceMappingURL=index.js.map