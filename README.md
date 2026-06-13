# CollabBoard 🚀

![Android CI](https://github.com/R0s3mrcx/collabboard/actions/workflows/android.yml/badge.svg)

> Connecting university students to find project collaborators and co-founders.

**Developer:** Fabricio Farro  
**Roles:** Product Owner · Tech Lead · Developer · QA/DevOps

---

## Problem Statement

Students with great project ideas can't find the right technical collaborators at their university. CollabBoard solves this by creating a focused space where students post projects and others can apply to join.

---

## Features

### Lab 1 & 2 — Core

1. 🔐 Email authentication with Firebase Auth (login + register)
2. 📋 Browse projects feed in real time with Firestore snapshot listener
3. ➕ Post your own project with title, description and tech stack
4. ✏️ Edit and delete your own projects (owner only)
5. 🔍 Search projects by title, tech stack or author
6. 🔄 Toggle project status between Open and Closed
7. 📄 View project details and send a join request
8. 👤 Personal profile with posted projects and logout
9. 📭 Empty state and loading indicators for better UX

### Lab 3 — Advanced Firebase (Cloud Functions)

10. 🔔 In-app notifications with real-time badge counter
11. 👥 Join request management — owners see all applicants, accept or reject
12. ⚡ 3 Cloud Functions (Node.js 22, 2nd Gen)
    - `onNewJoinRequest` — notifies owner when someone applies
    - `onRequestStatusChange` — notifies applicant when accepted/rejected
    - `onProjectStatusChange` — notifies applicants when project is closed
13. ✅ 30 unit tests passing (validators + business logic)

---

## Architecture

![Architecture Diagram](architecture.png)

```text
Android app (Kotlin)
├── LoginFragment              → Firebase Auth
├── FeedFragment               → Firestore real-time listener, search
├── DetailFragment             → Project detail, join request, owner controls
├── RequestsFragment           → Owner: view/accept/reject applicants [Lab 3]
├── NotificationsFragment      → In-app notifications [Lab 3]
└── ProfileFragment            → My projects (clickable → manage)

Firebase backend
├── Firebase Auth              → Email/password login
├── Cloud Firestore            → Projects, requests, notifications, users
└── Cloud Functions            → 3 server-side triggers [Lab 3]
```

---

## Firestore Data Model

| Collection | Fields |
|------------|---------|
| `users/` | uid, displayName, email, university, skills, createdAt |
| `projects/` | projectId, ownerId, ownerName, title, description, techStack, status, createdAt |
| `requests/` | requestId, projectId, projectTitle, applicantId, applicantName, message, status, createdAt |
| `notifications/` | notificationId, recipientId, message, projectId, projectTitle, applicantId, applicantName, type, isRead, createdAt |

**Notification types:** `join_request` · `request_accepted` · `request_rejected` · `project_closed`

---

## Tech Stack

`Kotlin` `Firebase Auth` `Cloud Firestore` `Cloud Functions (Node.js 22)` `Jetpack Navigation` `RecyclerView` `CardView` `GitHub Actions` `JUnit 4` `TypeScript`

---

## Cloud Functions — Deploy

```bash
cd functions
npm install
npm run build
cd ..
firebase deploy --only functions
```

Requires Firebase Blaze plan and Node.js 18+.

---

## Run Tests

```bash
./gradlew test
```

30 unit tests in `ValidatorsTest.kt` covering email, password, title, tech stack, description, search filter logic, status toggle logic, duplicate request detection, and notification badge visibility.

---

## Setup

1. Clone repo
2. Add `google-services.json` to `/app`
3. Enable Firebase Auth (Email/Password) and Firestore in Firebase Console
4. Run in Android Studio with API 24+
5. *(Optional)* Deploy Cloud Functions

---

## Sprint Board

CollabBoard Trello Board

---

## Lab Progress

| Lab | Status | Key Deliverables |
|------|--------|------------------|
| Lab 1 | ✅ Complete | Firebase setup, 4 screens, CI/CD, 9 tests |
| Lab 2 | ✅ Complete | Full CRUD, Search, Status Toggle, Empty State, Loading States, 13 tests |
| Lab 3 | ✅ Complete | Cloud Functions (3), Notifications, Applicant Management, 30 tests |