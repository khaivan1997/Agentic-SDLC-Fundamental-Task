# Task Manager Web App - Development Plan

## 📋 Project Overview
Build a full-stack Task Manager web application with CRUD operations using React (frontend) and Spring Boot (backend).

**Timeline:** 1-2 working days  
**Start Date:** February 22, 2026

---

## 🏗️ Architecture & Tech Stack

### Frontend
- **Framework:** React.js with TypeScript
- **Build Tool:** Vite (faster than CRA)
- **Styling:** TailwindCSS (lightweight, utility-first)
- **HTTP Client:** Axios or Fetch API
- **State Management:** React Hooks (useContext/Redux if needed)
- **Port:** http://localhost:5173 (Vite default)

### Backend
- **Framework:** Spring Boot (Java 17+)
- **Database:** H2 (in-memory for development), PostgreSQL (optional)
- **ORM:** Spring Data JPA
- **Build Tool:** Maven
- **Server Port:** http://localhost:8080
- **API Protocol:** REST with JSON

---

## 📁 Folder Structure

```
CDSLFreeAgent/
├── context/
│   ├── request.txt
│   ├── request.pdf
│   └── PLANNING.md
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── TaskList.tsx
│   │   │   ├── TaskForm.tsx
│   │   │   └── TaskCard.tsx
│   │   ├── services/
│   │   │   └── taskService.ts (API calls)
│   │   ├── types/
│   │   │   └── task.ts (TypeScript interfaces)
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
└── backend/
    ├── src/
    │   ├── main/java/com/taskmanager/
    │   │   ├── controller/
    │   │   │   └── TaskController.java
    │   │   ├── service/
    │   │   │   └── TaskService.java
    │   │   ├── repository/
    │   │   │   └── TaskRepository.java
    │   │   ├── model/
    │   │   │   └── Task.java
    │   │   ├── exception/
    │   │   │   └── ResourceNotFoundException.java
    │   │   └── config/
    │   │       └── CorsConfig.java
    │   └── resources/
    │       └── application.properties
    └── pom.xml
```

---

## 🔄 Development Phases

### Phase 1: Project Setup (30 min)
- [ ] Initialize Spring Boot backend project
- [ ] Initialize React + Vite frontend project
- [ ] Configure Maven dependencies
- [ ] Configure npm dependencies
- [ ] Set up Git repository

### Phase 2: Backend Development (45 min)
- [ ] Create Task entity and model
- [ ] Implement TaskRepository (JPA)
- [ ] Implement TaskService (business logic)
- [ ] Create TaskController (REST endpoints)
- [ ] Configure CORS
- [ ] Set up H2 database (DDL)
- [ ] Add input validation (Bean Validation)
- [ ] Test endpoints with Postman/cURL

### Phase 3: Frontend Development (60 min)
- [ ] Create TypeScript types/interfaces for Task
- [ ] Build TaskService (API calls with Axios)
- [ ] Create TaskList component (display all tasks)
- [ ] Create TaskForm component (add/edit task)
- [ ] Create TaskCard component (single task UI)
- [ ] Implement CRUD operations
- [ ] Add form validation
- [ ] Add error handling & messages
- [ ] Optional: Add sorting/filtering
- [ ] Style with TailwindCSS

### Phase 4: Integration & Testing (30 min)
- [ ] Connect frontend to backend
- [ ] Test all CRUD operations end-to-end
- [ ] Fix any CORS, API, or state management issues
- [ ] Test error scenarios

### Phase 5: Documentation & Cleanup (15 min)
- [ ] Document API endpoints
- [ ] Document setup & run instructions
- [ ] Clean up code, add comments
- [ ] Git commits with meaningful messages

---

## 🔌 REST API Endpoints

```
GET    /api/tasks           → Retrieve all tasks
GET    /api/tasks/{id}      → Retrieve a task by ID
POST   /api/tasks           → Create a new task
PUT    /api/tasks/{id}      → Update a task
DELETE /api/tasks/{id}      → Delete a task
```

### Request/Response Sample

**POST /api/tasks** (Create)
```json
{
  "title": "Complete project",
  "description": "Finish the Task Manager app",
  "status": "IN_PROGRESS",
  "dueDate": "2026-02-28"
}
```

**Response (201 Created)**
```json
{
  "id": 1,
  "title": "Complete project",
  "description": "Finish the Task Manager app",
  "status": "IN_PROGRESS",
  "dueDate": "2026-02-28"
}
```

---

## 🎯 Task Entity Details

```java
Task {
  Long id              // Auto-generated primary key
  String title         // Required, max 100 chars
  String description   // Optional, max 500 chars
  TaskStatus status    // Enum: TODO, IN_PROGRESS, DONE
  LocalDate dueDate    // Optional
}
```

---

## ✅ Success Criteria

- [x] All CRUD operations work end-to-end
- [x] Input validation on backend & frontend
- [x] Error messages displayed correctly
- [x] CORS enabled
- [x] Code is clean and documented
- [x] Git repository with meaningful commits
- [x] AI usage documented throughout

---

## 🚀 Next Steps

1. **Start Backend Setup** - Initialize Spring Boot project
2. **Start Frontend Setup** - Initialize React + Vite project
3. **Develop Backend API** - Implement entity, service, controller
4. **Develop Frontend UI** - Build React components
5. **Integrate & Test** - Connect frontend to backend
6. **Deploy (Optional)** - Vercel (frontend), Render/Fly.io (backend)

---

## 📝 Notes
- Use AI to help with code generation, debugging, and design decisions
- Commit frequently to Git with clear messages
- Document API requests/responses
- Test edge cases (empty fields, invalid inputs, etc.)
- Keep components reusable and modular

