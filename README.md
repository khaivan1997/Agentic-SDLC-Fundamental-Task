# Task Manager — Full-Stack Web Application

A full-stack task management application built with **Spring Boot**, **React (TypeScript)**, and **PostgreSQL**, extended with an **MCP Server** for AI agent integration.

---

## Architecture

```
[ React Frontend (Vite) ]
         ⇅ REST API (JSON)
[ Spring Boot Backend ]          [ MCP Server (SSE) ]
         ⇅                              ⇅
         └──── PostgreSQL (shared) ──────┘
```

| Module | Description |
| :--- | :--- |
| `frontend` | React + TypeScript SPA (Vite). CRUD UI for tasks. |
| `backend` | Spring Boot REST API. Full CRUD endpoints on `/api/tasks`. |
| `api-models` | Shared JPA entities (`Task`, `TaskStatus`) and `TaskRepository`. |
| `mcp-server` | Spring AI MCP Server. Exposes tools over SSE for AI agents. |

---

## Tech Stack

- **Java 17+**, Spring Boot 3.4, Spring Data JPA
- **React 18**, TypeScript, Vite
- **PostgreSQL 16** (via Docker) / H2 (for tests)
- **Spring AI 1.1.0-M2** (MCP Server with `@McpTool` annotations)
- **Maven** multi-module build

---

## Prerequisites

- Java 17+
- Node.js 20+ / npm 10+
- Docker (for PostgreSQL)
- Maven 3.9+

---

## Getting Started

### 1. Start PostgreSQL

```bash
docker compose up -d
```

This starts PostgreSQL on port **5436** with database `taskdb`, user `taskuser`, password `taskpass`.

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Backend

```bash
java -jar backend/target/backend-0.0.1-SNAPSHOT.war
```

The REST API is available at `http://localhost:8080/api/tasks`.

### 4. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

The UI is available at `http://localhost:5173`.

### 5. Run the MCP Server

```bash
java -jar mcp-server/target/mcp-server-0.0.1-SNAPSHOT.jar
```

The MCP SSE endpoint is available at `http://localhost:8081/sse`.

---

## REST API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tasks` | List all tasks |
| `GET` | `/api/tasks/{id}` | Get a task by ID |
| `POST` | `/api/tasks` | Create a new task |
| `PUT` | `/api/tasks/{id}` | Update an existing task |
| `DELETE` | `/api/tasks/{id}` | Delete a task |

### Task Entity

```json
{
  "id": 1,
  "title": "Example Task",
  "description": "Optional description (max 500 chars)",
  "status": "TODO",
  "dueDate": "2026-03-01"
}
```

Status values: `TODO`, `IN_PROGRESS`, `DONE`.

---

## MCP Server Tools

The MCP server exposes 4 tools via the Model Context Protocol (SSE transport):

| Tool Name | Description |
| :--- | :--- |
| `mcp-help` | Lists all available MCP tools and their descriptions. |
| `mcp-schema-tasks` | Returns the task table schema as JSON-Schema. |
| `mcp-tasks` | Bulk-inserts tasks into the shared PostgreSQL database. |
| `mcp-tasks-summary` | Returns task count statistics grouped by status. |

### Example AI Agent Prompt

> "Please inspect the task schema at `mcp-schema-tasks`. Then generate and insert 1000 diverse tasks with random statuses, titles, and due dates using `mcp-tasks`."

---

## Running Tests

```bash
# All modules
mvn clean test

# Backend only (14 tests)
mvn -pl backend test

# MCP Server only (unit + integration)
mvn -pl mcp-server test
```

---

## Environment Variables

| Variable | Default | Used By |
| :--- | :--- | :--- |
| `DB_URL` | `jdbc:postgresql://localhost:5436/taskdb` | backend, mcp-server |
| `DB_USERNAME` | `taskuser` | backend, mcp-server |
| `DB_PASSWORD` | `taskpass` | backend, mcp-server |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:5174` | backend |

---

## AI Usage

This project was developed collaboratively with AI tools. See [AI-USAGE.md](AI-USAGE.md) for a detailed log of the AI-assisted development workflow, including which tools were used, what they contributed, and critical reflections on the process.
