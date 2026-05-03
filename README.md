# CollabBoard 🚀
![Android CI](https://github.com/R0s3mrcx/collabboard/actions/workflows/android.yml/badge.svg)

> Connecting university students to find project collaborators and co-founders.

**Developer:** Fabricio Farro  
**Roles:** Product Owner · Tech Lead · Developer · QA/DevOps

## Problem Statement
Students with great project ideas can't find the right technical collaborators 
at their university. CollabBoard solves this by creating a focused space where 
students post projects and others can apply to join.

## MVP Features
1. 🔐 Email authentication with Firebase Auth (login + register)
2. 📋 Browse projects feed in real time with Firestore snapshot listener
3. ➕ Post your own project with title, description and tech stack
4. ✏️ Edit and delete your own projects (owner only)
5. 🔍 Search projects by title, tech stack or author
6. 🔄 Toggle project status between Open and Closed
7. 📄 View project details and send a join request
8. 👤 Personal profile with your posted projects and logout
9. 📭 Empty state when no projects exist
10. ⏳ Loading indicators on Feed and Detail screens

## Architecture
![Architecture Diagram](architecture.png)

## Firestore Data Model
**users/** — uid, displayName, email, university, skills, createdAt  
**projects/** — projectId, ownerId, ownerName, title, description, techStack, status, createdAt  
**requests/** — requestId, projectId, applicantId, applicantName, message, status, createdAt

## Tech Stack
`Kotlin` `Firebase Auth` `Firestore` `Navigation Component` `RecyclerView` `CardView` `GitHub Actions` `JUnit 4`

## Run Tests
```bash
./gradlew test
```
Tests: 13 unit tests in `ValidatorsTest.kt` covering email, password, 
title, tech stack and description validation.

## Setup
1. Clone repo
2. Add `google-services.json` to `/app`
3. Enable Firebase Auth (Email/Password) and Firestore in test mode
4. Run in Android Studio with API 24+

## Sprint Board
[CollabBoard Trello Board](https://trello.com/b/0iLxE3VL)

## Lab Progress
| Lab | Status | Key Deliverables |
|-----|--------|-----------------|
| Lab 1 | ✅ Complete | Firebase setup, 4 screens, CI/CD, 9 tests |
| Lab 2 | ✅ Complete | Full CRUD, Search, Status Toggle, Empty State, Loading States |
| Lab 3 | 🔄 In Progress | Advanced Firebase feature, usability testing |
