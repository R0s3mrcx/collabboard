# CollabBoard ๐€

![Android CI](https://github.com/R0s3mrcx/collabboard/actions/workflows/android.yml/badge.svg)

> Connecting university students to find project collaborators and co-founders.

**Developer:** Fabricio Farro  
**Roles:** Product Owner ยท Tech Lead ยท Developer ยท QA/DevOps

---

## Problem Statement

Students with great project ideas can't find the right technical collaborators at their university. CollabBoard solves this by creating a focused space where students post projects and others can apply to join.

---

## Features

### Lab 1 & 2 โ€” Core
1. ๐” Email authentication with Firebase Auth (login + register)
2. ๐“ Browse projects feed in real time with Firestore snapshot listener
3. โ• Post your own project with title, description and tech stack
4. โ๏ธ Edit and delete your own projects (owner only)
5. ๐” Search projects by title, tech stack or author
6. ๐” Toggle project status between Open and Closed
7. ๐“ View project details and send a join request
8. ๐‘ค Personal profile with posted projects and logout
9. ๐“ญ Empty state and loading indicators for better UX

### Lab 3 โ€” Advanced Firebase (Cloud Functions)
10. ๐”” In-app notifications with real-time badge counter
11. ๐‘ฅ Join request management โ€” owners see all applicants, accept or reject
12. โก 3 Cloud Functions (Node.js 22, 2nd Gen):
    - `onNewJoinRequest` โ€” notifies owner when someone applies
    - `onRequestStatusChange` โ€” notifies applicant when accepted/rejected
    - `onProjectStatusChange` โ€” notifies applicants when project is closed
13. โ… 30 unit tests passing (validators + business logic)

---

## Architecture

![Architecture Diagram](architecture.png)

```
Android app (Kotlin)
โ”โ”€โ”€ LoginFragment      โ’ Firebase Auth
โ”โ”€โ”€ FeedFragment       โ’ Firestore real-time listener, search
โ”โ”€โ”€ DetailFragment     โ’ Project detail, join request, owner controls
โ”โ”€โ”€ RequestsFragment   โ’ Owner: view/accept/reject applicants   [Lab 3]
โ”โ”€โ”€ NotificationsFragment โ’ In-app notifications               [Lab 3]
โ””โ”€โ”€ ProfileFragment    โ’ My projects (clickable โ’ manage)

Firebase backend
โ”โ”€โ”€ Firebase Auth      โ’ Email/password login
โ”โ”€โ”€ Cloud Firestore    โ’ Projects, requests, notifications, users
โ””โ”€โ”€ Cloud Functions    โ’ 3 server-side triggers                [Lab 3]
```

---

## Firestore Data Model

| Collection | Fields |
|---|---|
| `users/` | uid, displayName, email, university, skills, createdAt |
| `projects/` | projectId, ownerId, ownerName, title, description, techStack, status, createdAt |
| `requests/` | requestId, projectId, projectTitle, applicantId, applicantName, message, status, createdAt |
| `notifications/` | notificationId, recipientId, message, projectId, projectTitle, applicantId, applicantName, type, isRead, createdAt |

**Notification types:** `join_request` ยท `request_accepted` ยท `request_rejected` ยท `project_closed`

---

## Tech Stack

`Kotlin` `Firebase Auth` `Cloud Firestore` `Cloud Functions (Node.js 22)` `Jetpack Navigation` `RecyclerView` `CardView` `GitHub Actions` `JUnit 4` `TypeScript`

---

## Cloud Functions โ€” Deploy

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
3. Enable **Firebase Auth** (Email/Password) and **Firestore** in Firebase Console
4. Run in Android Studio with API 24+
5. *(Optional)* Deploy Cloud Functions โ€”

---

## Sprint Board

[CollabBoard Trello Board](https://trello.com/b/0iLxE3VL)

---

## Lab Progress

| Lab | Status | Key Deliverables |
|---|---|---|
| Lab 1 | โ… Complete | Firebase setup, 4 screens, CI/CD, 9 tests |
| Lab 2 | โ… Complete | Full CRUD, Search, Status Toggle, Empty State, Loading States, 13 tests |
| Lab 3 | โ… Complete | Cloud Functions (3), Notifications, Applicant Management, 30 tests |
