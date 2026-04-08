# StudyMore

> A gamified desktop study application that makes learning rewarding.

StudyMore helps students build productive study habits through gamification — earn coins, build streaks, climb ranks, unlock achievements, and compete with friends on leaderboards. Built with JavaFX and a Spring Boot backend.

---

## Team — CodeMaxxers

| Name | Role |
|---|---|
| Alper Gezer | Study Engine & Rewards Logic |
| Tunar Babali | Social Systems & Achievements |
| Kutay Görür | Productivity & SRS Algorithms |
| Mete Namazov | User Auth & Account Infrastructure |
| Muhammad Hassaan Aslam | Economy & Customization (Shop) |

---

## Features

- **Timer** — Customizable study/break intervals with a real-time multiplier that grows the longer you stay focused
- **Coin & Rank System** — Earn coins each session based on duration and multiplier: from Bronze to Grandmaster
- **Achievements** — 25 unlockable milestones across time, streak, social, task, and coin categories with popup notifications
- **Spaced Repetition (SRS)** — Smart task scheduling that surfaces cards at optimal review intervals
- **Friends & Study Groups** — Add friends, create groups, and compete on a shared leaderboard
- **Online Presence** — See who's currently studying with real-time online/offline status
- **Shop & Inventory** — Spend coins on mascot skins, banners, backgrounds, titles, and more
- **Mascot Cat** — A customizable companion with motivational quotes during sessions
- **Offline Support** — Study locally with SQLite. Data syncs to the cloud when back online

---

## Architecture

StudyMore uses a **three-tier MVC architecture** with a hybrid local/cloud data model:

```
┌─────────────────────────────────────────────┐
│              Client (JavaFX)                │
│  View Layer → Controller Layer → Model Layer│
│                    ↕ REST API               │
├─────────────────────────────────────────────┤
│           Server (Spring Boot)              │
│     REST Controllers → Service Layer        │
│                    ↕                        │
├─────────────────────────────────────────────┤
│         Database (PostgreSQL/Neon)          │
│  Users · Friends · Groups · Achievements   │
└─────────────────────────────────────────────┘
```

| Data | Storage | Reason |
|---|---|---|
| Sessions, tasks, shop, inventory | SQLite (local) | Fast, offline-capable |
| Users, friends, groups, online status | PostgreSQL via Neon | Shared globally |

---

## Tech Stack

### Frontend
- **Java 21** with **JavaFX 21**
- **SQLite** for local offline storage
- **Maven** for build management

### Backend
- **Spring Boot 3** — REST API
- **Spring Data JPA / Hibernate** — ORM layer
- **PostgreSQL (Neon)** — Cloud database
- **Railway** — Backend hosting at `https://studymore-production.up.railway.app`

---

## Project Structure

```
StudyMore/
├── studymore-frontend/          # JavaFX desktop application
│   └── src/main/java/StudyMore/
│       ├── controllers/         # FXML controllers (Study, Friends, Profile...)
│       ├── models/              # Domain models (User, StudySession, Task...)
│       ├── db/                  # DatabaseManager (SQLite)
│       └── Main.java            # Entry point
│
└── studymore-backend/           # Spring Boot REST API
    └── src/main/java/com/codemaxxers/
        ├── controller/          # REST controllers
        ├── service/             # Business logic
        ├── model/               # JPA entities
        └── repository/          # Spring Data repositories
```

---

## Key API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/users/sync` | Register/sync user with cloud |
| `POST` | `/auth/login` | Authenticate with username + password hash |
| `POST` | `/auth/users/heartbeat` | Update online status |
| `GET` | `/api/friends/{userId}` | Get friend list |
| `POST` | `/api/friends/requests` | Send a friend request |
| `PUT` | `/api/friends/requests/{id}/accept` | Accept a friend request |
| `GET` | `/api/groups/user/{userId}` | Get user's study groups |
| `GET` | `/api/groups/{groupId}/leaderboard` | Get group leaderboard |
| `POST` | `/api/groups` | Create a study group |

---

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.8+

### Run the Frontend

```bash
cd studymore-frontend
mvn javafx:run
```

### Run the Backend (local)

```bash
cd studymore-backend
mvn spring-boot:run
```

> The backend is already deployed on Railway. The frontend connects to it automatically — no local backend setup required for normal usage.

---

## Database

- **Local:** SQLite database file created automatically at `studymore_database.db` on first launch
- **Cloud:** PostgreSQL hosted on [Neon](https://neon.tech), connected via Railway

---

## CS102 — Spring 2025/2026
Bilkent University · Instructor: Uğur Güdükbay · Teacher Assistant: Ali Erdem Karaçay