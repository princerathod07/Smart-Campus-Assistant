# 🏛️ Smart Campus Assistant System (SCAS)
### Complete Full-Stack Java Web Application — University Project

---

## 📋 TABLE OF CONTENTS
1. [System Architecture](#architecture)
2. [Java OOP Concepts Map](#oop-concepts)
3. [System Workflow Flowchart](#flowchart)
4. [Project Folder Structure](#structure)
5. [Setup & Installation](#setup)
6. [Running Locally](#running)
7. [Feature Guide](#features)
8. [Data Storage](#storage)

---

## 🏗️ SYSTEM ARCHITECTURE {#architecture}

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SMART CAMPUS ASSISTANT SYSTEM                     │
│                         System Architecture                          │
└─────────────────────────────────────────────────────────────────────┘

  ┌──────────────────────────────────────────────────────────────────┐
  │                        PRESENTATION TIER                          │
  │  HTML5 + CSS3 + JavaScript (Responsive Dashboard)                │
  │                                                                    │
  │  ┌─────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────┐  │
  │  │  Login  │ │Dashboard │ │ Library  │ │Complaint │ │AI Chat │  │
  │  │Register │ │Timetable │ │  Search  │ │Submission│ │  Bot   │  │
  │  └────┬────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └───┬────┘  │
  └───────┼───────────┼────────────┼─────────────┼───────────┼───────┘
          │           │            │             │           │
          ▼           ▼            ▼             ▼           ▼
  ┌──────────────────────────────────────────────────────────────────┐
  │                     HTTP / AJAX REQUESTS                          │
  │                    (GET/POST to /api/*)                           │
  └──────────────────────────────────────────────────────────────────┘
          │
          ▼
  ┌──────────────────────────────────────────────────────────────────┐
  │                    APPLICATION TIER (Java)                        │
  │                    Apache Tomcat 10+                              │
  │                                                                   │
  │  ┌────────────────────────┐  ┌─────────────────────────────────┐ │
  │  │     AuthServlet        │  │         ApiServlet              │ │
  │  │  /api/auth/*           │  │         /api/*                  │ │
  │  │  - login               │  │  - /library   - /timetable      │ │
  │  │  - register            │  │  - /complaints- /announcements  │ │
  │  │  - logout              │  │  - /chat      - /notifications  │ │
  │  │  - session check       │  │  - /status                      │ │
  │  └────────────────────────┘  └─────────────────────────────────┘ │
  │            │                              │                       │
  │            ▼                              ▼                       │
  │  ┌──────────────────────────────────────────────────────────────┐ │
  │  │               SERVICE LAYER (Interfaces + Classes)           │ │
  │  │                                                              │ │
  │  │  CampusService (Interface)                                   │ │
  │  │       ├── LibraryService      (books search, catalogue)      │ │
  │  │       ├── ComplaintService    (submit, track, update)        │ │
  │  │       └── AnnouncementService (post, view, filter)           │ │
  │  └──────────────────────────────────────────────────────────────┘ │
  │            │                                                       │
  │            ▼                                                       │
  │  ┌──────────────────────────────────────────────────────────────┐ │
  │  │               USER DOMAIN (OOP Hierarchy)                    │ │
  │  │                                                              │ │
  │  │       User (abstract)                                        │ │
  │  │        ├── Student  (Inheritance)                            │ │
  │  │        └── Admin    (Inheritance + different polymorphism)   │ │
  │  └──────────────────────────────────────────────────────────────┘ │
  │            │                                                       │
  │            ▼                                                       │
  │  ┌──────────────────────────────────────────────────────────────┐ │
  │  │                UTILITIES & THREADS                           │ │
  │  │                                                              │ │
  │  │   FileManager (Singleton)     NotificationThread (Runnable)  │ │
  │  │   - readLines()               - run()                        │ │
  │  │   - writeLines()              - pushNotification()           │ │
  │  │   - appendLine()              - drainNotifications()         │ │
  │  └──────────────────────────────────────────────────────────────┘ │
  └──────────────────────────────────────────────────────────────────┘
          │
          ▼
  ┌──────────────────────────────────────────────────────────────────┐
  │                      DATA TIER (File Storage)                     │
  │                                                                   │
  │  /data/users.txt          → Pipe-delimited user records          │
  │  /data/books.txt          → Library catalogue                    │
  │  /data/complaints.txt     → Student complaints                   │
  │  /data/announcements.txt  → Campus announcements                 │
  └──────────────────────────────────────────────────────────────────┘
```

---

## 📚 JAVA OOP CONCEPTS MAP {#oop-concepts}

| Concept | Where Used | File |
|---------|-----------|------|
| **Classes & Objects** | `User`, `Student`, `Admin`, `FileManager` | `users/` package |
| **Constructors** | Overloaded constructors in all User classes | `User.java`, `Student.java`, `Admin.java` |
| **Encapsulation** | Private fields + getters/setters in all domain classes | All `users/` classes |
| **Inheritance** | `Student extends User`, `Admin extends User` | `Student.java`, `Admin.java` |
| **Abstract Classes** | `User` is abstract with abstract methods | `User.java` |
| **Interfaces** | `CampusService` implemented by 3 service classes | `services/CampusService.java` |
| **Polymorphism / Dynamic Method Dispatch** | `getRole()`, `getDashboardMessage()` overridden differently in Student vs Admin | `Student.java`, `Admin.java` |
| **Packages** | `com.campus.users`, `services`, `utils`, `exceptions`, `servlets` | All source files |
| **Exception Handling** | `LoginException`, `FileStorageException`, try-catch in all servlets | `exceptions/` package |
| **Multithreading** | `NotificationThread implements Runnable`, `synchronized`, `volatile`, `ScheduledExecutorService` | `utils/NotificationThread.java` |
| **File Handling** | `FileManager` reads/writes all `.txt` data files | `utils/FileManager.java` |
| **Singleton Pattern** | `FileManager.getInstance()` | `FileManager.java` |
| **Collections** | `List<Map<>>`, `Queue`, `ArrayList` throughout | Service classes |

---

## 🔄 SYSTEM WORKFLOW FLOWCHART {#flowchart}

```
                        ┌─────────────────────┐
                        │    USER OPENS APP    │
                        └──────────┬──────────┘
                                   │
                        ┌──────────▼──────────┐
                        │   Session Active?    │
                        └──────────┬──────────┘
                         YES ◄─────┤──────► NO
                          │        │           │
                          ▼        │           ▼
                   ┌──────────┐    │  ┌─────────────────┐
                   │Dashboard │    │  │  Login / Register│
                   └──────────┘    │  └────────┬────────┘
                                   │           │
                                   │  ┌────────▼────────┐
                                   │  │Validate Input   │
                                   │  │(Exception check)│
                                   │  └────────┬────────┘
                                   │       VALID│    INVALID
                                   │           │       │
                                   │           │  ┌────▼──────┐
                                   │           │  │LoginExcep.│
                                   │           │  │Show Error │
                                   │           │  └───────────┘
                                   │  ┌────────▼────────┐
                                   │  │ Create Session  │
                                   └──│ Redirect to App │
                                      └────────┬────────┘
                                               │
                              ┌────────────────▼──────────────────┐
                              │           DASHBOARD                │
                              │  (Stats, Announcements, Today's    │
                              │   Schedule loaded asynchronously)  │
                              └──────────────────┬────────────────┘
                                                  │
              ┌───────────────┬──────────┬───────┴────────┬──────────┐
              │               │          │                │          │
        ┌─────▼──────┐ ┌──────▼───┐ ┌───▼────┐ ┌────────▼─┐ ┌──────▼──┐
        │ TIMETABLE  │ │ LIBRARY  │ │ANNOUNCE│ │COMPLAINTS│ │ AI BOT  │
        │            │ │          │ │MENTS   │ │          │ │         │
        │ Load from  │ │ File read│ │File    │ │Submit    │ │Keyword  │
        │ static     │ │ books.txt│ │ann.txt │ │Save to   │ │matching │
        │ timetable  │ │ Search   │ │Filter  │ │file      │ │Response │
        │ data       │ │ filter   │ │by prio │ │Track     │ │from KB  │
        └────────────┘ └──────────┘ └────────┘ └──────────┘ └─────────┘
                                                 
              ┌─────────────────────────────────────────────────────┐
              │              BACKGROUND THREAD (Multithreading)      │
              │   NotificationThread polls every 30s                 │
              │   Enqueues HIGH priority announcements               │
              │   Synchronized queue access (thread safety)          │
              └─────────────────────────────────────────────────────┘
```

---

## 📁 PROJECT FOLDER STRUCTURE {#structure}

```
SmartCampusAssistant/
│
├── pom.xml                                    ← Maven build config
├── README.md                                  ← This file
│
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── campus/
│                   │
│                   ├── users/                 ← OOP Domain Model
│                   │   ├── User.java          ← Abstract base class
│                   │   ├── Student.java       ← Extends User
│                   │   └── Admin.java         ← Extends User
│                   │
│                   ├── services/              ← Business Logic
│                   │   ├── CampusService.java ← Interface
│                   │   ├── LibraryService.java
│                   │   ├── ComplaintService.java
│                   │   └── AnnouncementService.java
│                   │
│                   ├── utils/                 ← Utilities
│                   │   ├── FileManager.java   ← File I/O (Singleton)
│                   │   └── NotificationThread.java ← Multithreading
│                   │
│                   ├── exceptions/            ← Custom Exceptions
│                   │   ├── LoginException.java
│                   │   └── FileStorageException.java
│                   │
│                   └── servlets/              ← HTTP Controllers
│                       ├── AuthServlet.java   ← /api/auth/*
│                       └── ApiServlet.java    ← /api/*
│
└── web/                                       ← Frontend
    ├── index.html                             ← Complete SPA Frontend
    ├── WEB-INF/
    │   └── web.xml                            ← Servlet config
    └── data/                                  ← File Storage
        ├── users.txt
        ├── books.txt
        ├── complaints.txt
        └── announcements.txt
```

---

## 🚀 SETUP & INSTALLATION {#setup}

### Prerequisites
- Java JDK 17 or higher
- Apache Tomcat 10.1+ (supports Jakarta EE 9+)
- Maven 3.8+
- Any IDE: IntelliJ IDEA / Eclipse / VS Code

### Step 1 — Install Java
```bash
# Check Java version
java -version
# Should output: openjdk 17.x.x or higher
```

### Step 2 — Download Apache Tomcat
1. Go to https://tomcat.apache.org/download-10.cgi
2. Download Tomcat 10.1.x (Core > zip)
3. Extract to a folder, e.g., `C:/tomcat` or `~/tomcat`

### Step 3 — Build the Project
```bash
# Clone or copy the project folder
cd SmartCampusAssistant

# Build with Maven
mvn clean package

# This generates: target/SmartCampusAssistant.war
```

### Step 4 — Deploy to Tomcat
```bash
# Copy WAR to Tomcat webapps folder
cp target/SmartCampusAssistant.war /path/to/tomcat/webapps/

# Start Tomcat
/path/to/tomcat/bin/startup.sh    # Linux/Mac
/path/to/tomcat/bin/startup.bat   # Windows
```

### Step 5 — Open Browser
```
http://localhost:8080/SmartCampusAssistant/
```

---

## 💻 RUNNING LOCALLY (Quick Test — Frontend Only) {#running}

If you just want to test the **frontend** without the Java backend:

1. Open `web/index.html` directly in any browser
2. The JavaScript simulates all backend APIs in-memory
3. Use demo credentials:
   - **Student**: username `student` / password `student123`
   - **Admin**: username `admin` / password `admin123`

The in-browser demo includes:
- ✅ Login & Registration with validation
- ✅ Dashboard with live stats
- ✅ Complete library book catalogue
- ✅ Complaint submission & tracking
- ✅ Campus announcements (admin can post)
- ✅ AI ChatBot (keyword-based knowledge base)
- ✅ Weekly timetable
- ✅ Toast notifications & background polling

---

## 🌟 FEATURES GUIDE {#features}

### 1. User Authentication
- Login with username/password (SHA-256 hashed)
- Student registration with department, semester, program
- Session management via `HttpSession`
- `LoginException` thrown for invalid credentials

### 2. Student Dashboard
- Summary statistics (books, complaints, announcements)
- Today's timetable preview
- Latest 4 announcements widget
- Live clock display

### 3. Library System
- 10 pre-seeded books in `books.txt`
- Search by title, author, or category
- Available/unavailable status with copy count
- File-backed with `LibraryService`

### 4. Complaint Management
- Category selection (9 types)
- Description validation (min 10 chars)
- Stored in `complaints.txt`
- Status tracking: PENDING → REVIEWING → RESOLVED

### 5. Announcements
- 5 pre-seeded announcements
- Priority levels: HIGH (red), MEDIUM (amber), LOW (green)
- Admin users can post new announcements
- Stored in `announcements.txt`

### 6. AI Campus Assistant (CampusBot)
- Keyword-based question answering
- Covers: library, timetable, cafeteria, WiFi, fees, hostel, medical, sports
- Quick-reply chips for common topics
- Typing indicator simulation

### 7. Background Notifications (Multithreading)
- `NotificationThread` polls every 30 seconds
- Enqueues HIGH priority announcements
- Thread-safe `synchronized` queue
- Daemon thread (doesn't block JVM shutdown)

---

## 🗄️ DATA STORAGE FORMAT {#storage}

### users.txt (pipe-delimited)
```
userId|username|passwordHash|email|fullName|role|registeredAt|studentId|department|semester|program
```

### books.txt
```
id|title|author|isbn|category|available|copies
```

### complaints.txt
```
id|studentId|studentName|category|description|status|timestamp
```

### announcements.txt
```
id|title|content|author|priority|timestamp
```

---

## 📊 OOP CONCEPT DEMONSTRATION SUMMARY

| Java Concept | Implementation | Grade Impact |
|---|---|---|
| **Classes & Objects** | User, Student, Admin, FileManager, etc. | ✅ Core |
| **Constructors** | 3 constructor types in User hierarchy | ✅ Core |
| **Inheritance** | Student → User, Admin → User | ✅ Core |
| **Interfaces** | CampusService → 3 implementations | ✅ Core |
| **Polymorphism** | getRole(), getDashboardMessage() dynamic dispatch | ✅ Core |
| **Encapsulation** | Private fields, public getters/setters | ✅ Core |
| **Abstract Classes** | Abstract User with abstract methods | ✅ Core |
| **Packages** | com.campus.{users,services,utils,exceptions,servlets} | ✅ Core |
| **Exception Handling** | LoginException, FileStorageException, try-catch | ✅ Core |
| **File Handling** | FileManager reads/writes all .txt files | ✅ Core |
| **Multithreading** | NotificationThread, synchronized, volatile | ✅ Core |
| **Servlets** | AuthServlet, ApiServlet | ✅ Full-Stack |

---

*Smart Campus Assistant System — Full-Stack Java University Project*  
*Demonstrates all required OOP concepts with a production-quality web interface*
