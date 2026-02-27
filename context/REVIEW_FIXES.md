# REVIEW.md — Fix Tracker

**Original Review:** 2026-02-25  
**Last Checked:** 2026-02-27  

Tracks only the issues flagged in `REVIEW.md`.

---

## ✅ FIXED

### 1. No project-level README.md (L70–77) ✅
- **Issue:** No README; developer cannot run the project without prior knowledge.
- **Fix:** Root `README.md` created with prerequisites, build/run instructions, API reference, environment variables, and test commands.

### 2. frontend/README.md is default Vite template (L71) ✅
- **Issue:** Only README was the unmodified Vite scaffold.
- **Fix:** Root `README.md` now serves as the single project doc. Vite README remains but is no longer the only one.

### 3. `react-router-dom` unused (L101) ✅
- **Fix:** Removed from `package.json` entirely.

### 4. `axios` in `devDependencies` (L102) ✅
- **Fix:** Moved to `dependencies` (`"axios": "^1.13.5"`).

### 5. Zero frontend tests (L52–60) ✅
- **Issue:** 0 frontend tests; recommended 15–25 using Vitest + RTL.
- **Current:** 16 tests in `TaskList.test.tsx`:

| Area | Covered Scenarios |
|---|---|
| Basic rendering | Fetch + grouped columns |
| Search/filter | Keyword filtering + no-match state |
| Create flow | Happy path + API field error handling |
| Form validation | Required title, title length, description length |
| Edit flow | Enter edit mode, save update, cancel edit |
| Status flow | Status dropdown update + API call |
| Delete flow | Confirmed delete, cancelled delete, delete API failure |
| States/sorting | Fetch failure state, empty state, sort by due date |

- **Infrastructure:** Vitest 4.0 + React Testing Library + jsdom fully configured.
- **Status:** ✅ Fixed (test depth now in recommended range).

---

## ✅ VERIFIED STATUS

### 6. `spring.jpa.open-in-view` warning (L121) ✅
- **Issue:** Not explicitly set, causes startup WARN log.
- **Fix:** Added `spring.jpa.open-in-view=false` in:
  - `backend/src/main/resources/application.properties`
  - `backend/src/test/resources/application.properties`
- **Status:** ✅ Fixed.

### 7. H2 console enabled (L122)
- **Issue:** `spring.h2.console.enabled=true` — security concern for production.
- **Status:** ⚠️ Removed from production `application.properties` (PostgreSQL migration). Still present in test config, which is correct.

### 8. No service-layer unit tests (L49) ✅
- **Issue:** Only integration tests; no `TaskServiceTest` with mocked repository.
- **Fix:** Added `backend/src/test/java/com/taskmanager/service/TaskServiceTest.java` (Mockito + JUnit 5) covering:
  - `getAllTasks` returns repository data
  - `getTaskById` throws `ResourceNotFoundException` when missing
  - `createTask` defaults null status to `TODO`
  - `updateTask` applies updates and defaults null status to `TODO`
  - `deleteTask` resolves by id and calls repository delete
- **Status:** ✅ Fixed.

### 9. WAR packaging not documented (L17)
- **Issue:** `BackendApplication` extends `SpringBootServletInitializer` / WAR — not obvious how to deploy.
- **Status:** ✅ Root `README.md` now documents `java -jar backend/target/backend-0.0.1-SNAPSHOT.war`.

---

## 📊 Grade Impact

| Area | Original | Now | Change |
|------|----------|-----|--------|
| **Architecture** | A | A | — |
| **Testing** | C | B | 16 frontend tests added (was 0) |
| **Documentation** | D | B+ | README.md created |
| **Frontend Functionality** | A | A | — |
| **Backend API** | A | A | H2 console removed from prod |
| **Code Execution** | A | A | — |
| **Overall** | **B** | **A-** | +docs, +broader frontend tests, +dep fixes, +service unit tests |

---

## Remaining to reach A

No unresolved items from `REVIEW.md` remain in this tracker.
