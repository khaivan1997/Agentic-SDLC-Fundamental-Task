# Code Review — MCP Server Implementation

**Review Date:** 2026-03-05 (updated after fixes)  
**Spec:** `context/request-ext.txt` (Accenture Agentic SDLC Advanced)

---

## 1. Functional Requirements Coverage

| Requirement (from spec) | Implementation | Status |
|---|---|---|
| `mcp-schema-tasks` — returns JSON-Schema for tasks table | `TaskMcpTools.schemaTasks()` returns `Map` with `table`, `type`, `required`, `properties`. Uses `Task.TITLE_MAX_LENGTH` / `DESCRIPTION_MAX_LENGTH` constants. | ✅ |
| `mcp-tasks` — accepts JSON array, bulk inserts | `TaskMcpTools.insertTasks(List<TaskInput>)` with `@Transactional`. Per-item validation, partial insert, error reporting. | ✅ |
| `mcp-tasks-summary` — returns count stats per status | `TaskMcpTools.tasksSummary()` returns `TaskSummary` with `total` and `byStatus` map. | ✅ |
| `mcp-help` — agent-readable tool descriptions | `TaskMcpTools.help()` returns map of all 4 tool names + descriptions. | ✅ |

**All 4 required tools implemented.**

---

## 2. Success Criteria (spec §Success Criteria)

| # | Criterion | Evidence | Status |
|---|-----------|----------|--------|
| 1 | MCP Tool running, respects spec 2025-06-18 | SSE endpoint at `/sse`, `McpSchema.LATEST_PROTOCOL_VERSION` asserted to `"2025-06-18"` in `McpServerIntegrationTests`. Protocol handshake test in `McpProtocolHandshakeIT` validates negotiated version. | ✅ |
| 2 | Schema inspection via `mcp-schema-tasks` | Unit test verifies all fields, types, constraints, enum values. Protocol test calls it over SSE. | ✅ |
| 3 | AI agent inserts 1000 records via `mcp-tasks` | Bulk insert tested with 1, 10, and 50 records (protocol test). No 1000-record automated test. | ⚠️ PARTIAL |
| 4 | Summary reflects inserted data | Unit tests verify counts. Protocol test calls summary after insert. | ✅ |
| 5 | All actions documented | README.md documents MCP tools, sample prompts, and running instructions. | ✅ |

---

## 3. Architecture Review

### ✅ Strengths
- **Clean module separation**: `mcp-server` depends on `api-models` for shared `Task`, `TaskStatus`, `TaskRepository` — no entity duplication.
- **Correct component scanning**: `@EntityScan("com.taskmanager.model")` and `@EnableJpaRepositories("com.taskmanager.repository")` since `McpServerApplication` is in `com.taskmanager.mcp`.
- **Shared DB**: Same PostgreSQL connection config (env-configurable) as the main backend.
- **Separate port**: `server.port=8081` avoids conflict with backend on 8080.
- **Constructor injection**: Used consistently in `TaskMcpTools`.
- **Annotation-based MCP**: `@McpTool` / `@McpToolParam` from Spring AI community — auto-registered with the MCP protocol handler.

### ✅ Previously Flagged — Now Fixed
- ~~`spring.jpa.open-in-view` not set~~ → ✅ Set to `false` in both prod and test properties.
- ~~`spring.jpa.show-sql=true` in production~~ → ✅ Set to `false`.

### ⚠️ Remaining Observation
- **No health or readiness endpoint** — makes it harder to validate MCP server status in orchestrated environments.

---

## 4. Code Quality

### `TaskMcpTools.java` (146 lines)

| Area | Assessment |
|---|---|
| **Validation** | ✅ Thorough — null task, blank title, title length, description length, invalid status enum |
| **Input sanitization** | ✅ `trim()` on title, `normalizeDescription()` trims or nulls blank descriptions |
| **Locale-safe parsing** | ✅ `toUpperCase(Locale.ROOT)` in `parseStatus()` |
| **Partial insert** | ✅ Valid tasks saved even when some are rejected; errors collected with index |
| **Transaction safety** | ✅ `@Transactional` on `insertTasks` |
| **Response structure** | ✅ Returns `received`, `inserted`, `rejected`, `totalInDatabase`, and optional `errors` |
| **Schema accuracy** | ✅ Uses `Task.TITLE_MAX_LENGTH` / `DESCRIPTION_MAX_LENGTH` constants; enum values derived from `TaskStatus.values()` |

~~**Previously flagged:** `schemaTasks()` hard-coded enum values.~~ → ✅ Fixed — now derived from `TaskStatus.values()`.

### DTOs

| Class | Assessment |
|---|---|
| `TaskInput` | Simple POJO with `title`, `description`, `status` (String), `dueDate` (LocalDate). No validation annotations needed — validation is done in `TaskMcpTools.validate()`. Clean. |
| `TaskSummary` | `total` (long) + `byStatus` (Map). Default constructor present for serialization. Clean. |

---

## 5. Test Coverage Analysis

### Unit Tests: `TaskMcpToolsTest.java` — 13 tests

| # | Test | Tool Covered | What it verifies |
|---|------|-------------|------------------|
| 1 | `help_returnsAllToolDescriptions` | `mcp-help` | All 4 tool names present |
| 2 | `schemaTasks_returnsExpectedSchemaShape` | `mcp-schema-tasks` | All fields, maxLength, enum values |
| 3 | `insertTasks_withSingleTask_insertsAndReturnsCounts` | `mcp-tasks` | Single valid task saved, counts correct |
| 4 | `insertTasks_withoutStatus_defaultsToTodo` | `mcp-tasks` | Null status → TODO |
| 5 | `insertTasks_withTenTasks_insertsAll` | `mcp-tasks` | Batch of 10, all saved |
| 6 | `insertTasks_withInvalidTask_rejectsAndDoesNotSave` | `mcp-tasks` | Blank title rejected, `saveAll` not called |
| 7 | `tasksSummary_returnsCountsFromRepository` | `mcp-tasks-summary` | Correct total + per-status |
| 8 | `tasksSummary_emptyDatabase_returnsZeroCounts` | `mcp-tasks-summary` | All zeros |
| 9 | `insertTasks_withNull_returnsZeroCounts` | `mcp-tasks` | Null input returns zeros, no DB call |
| 10 | `insertTasks_withEmptyList_returnsZeroCounts` | `mcp-tasks` | Empty list returns zeros, no DB call |
| 11 | `insertTasks_mixedValidAndInvalid_savesOnlyValid` | `mcp-tasks` | Partial insert: valid saved, invalid rejected |
| 12 | `insertTasks_withDescriptionTooLong_rejects` | `mcp-tasks` | Description > 500 chars rejected |
| 13 | `insertTasks_withInvalidStatus_rejects` | `mcp-tasks` | Invalid enum value rejected |

**Assessment:** All 4 tools covered with comprehensive variety including edge cases. Uses `ArgumentCaptor` to verify saved entities. Mockito mocks ensure isolation from the DB.

### Integration Tests: `McpServerIntegrationTests.java` — 4 tests

| # | Test | What it verifies |
|---|------|------------------|
| 1 | `mcpServer_startsAndExposesSseEndpoint` | GET `/sse` returns 200 with SSE media type |
| 2 | `mcpServer_metadataConfigured` | Server name is `task-manager-mcp`, SSE endpoint is `/sse` |
| 3 | `mcpModelClassesAreLoadable` | `TaskInput` DTO is instantiable |
| 4 | `mcpProtocol_supportedVersionFromSdk` | `McpSchema.LATEST_PROTOCOL_VERSION == "2025-06-18"` |

**Assessment:** Verifies the Spring context loads with MCP configuration and the SSE endpoint is reachable. Test #3 is trivial (could be removed without loss). Test #4 validates spec compliance at the SDK level.

### Protocol Tests: `McpProtocolHandshakeIT.java` — 3 tests (gated)

| # | Test | What it verifies |
|---|------|------------------|
| 1 | `initializeNegotiatesSdkProtocolVersion` | Full MCP handshake negotiates `"2025-06-18"` |
| 2 | `mcpTools_areDiscoverableAndCallableOverProtocol` | `listTools` returns all 4 tools; `mcp-schema-tasks` and `mcp-tasks-summary` callable without error |
| 3 | `mcpTasks_bulkInsertAndSummaryOverProtocol` | Inserts 50 tasks over MCP protocol, then calls summary |

**Assessment:** These are the most valuable tests — they exercise the full MCP client→server→DB pipeline. They are gated behind `-Dmcp.handshake.test=true` because they require a real running server (RANDOM_PORT), which is reasonable.

### Remaining Test Gaps

| Gap | Severity | Description |
|---|---------|-------------|
| No 1000-record insert test | ⚠️ Medium | Spec explicitly requires inserting 1000 records. The 10 and 50 record tests validate the path, but a 1000-record test would prove spec compliance. |
| No test for `mcp-help` over protocol | Low | `McpProtocolHandshakeIT` calls `mcp-schema-tasks`, `mcp-tasks`, `mcp-tasks-summary` over protocol but skips `mcp-help`. |

**Previously flagged — now covered:**
- ~~Null/empty list input~~ → ✅ `insertTasks_withNull`, `insertTasks_withEmptyList`
- ~~Mixed valid+invalid batch~~ → ✅ `insertTasks_mixedValidAndInvalid_savesOnlyValid`
- ~~Description too long~~ → ✅ `insertTasks_withDescriptionTooLong_rejects`
- ~~Invalid status~~ → ✅ `insertTasks_withInvalidStatus_rejects`

---

## 6. Total Test Count

| Test Class | Tests | Type | Run by default? |
|---|---|---|---|
| `TaskMcpToolsTest` | 13 | Unit (Mockito) | ✅ Yes |
| `McpServerIntegrationTests` | 4 | Integration (Spring Boot) | ✅ Yes |
| `McpProtocolHandshakeIT` | 3 | Protocol (MCP Client) | ❌ Only with `-Dmcp.handshake.test=true` |
| **Total** | **20** | | **17 default, 3 gated** |

---

## 7. Build Verification

```
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

✅ All 17 default tests pass. `spring.jpa.open-in-view` warning no longer appears in output.

---

## 📊 Grading

| Area | Grade | Notes |
|------|-------|-------|
| **Spec compliance** | **A-** | All 4 tools implemented correctly. MCP 2025-06-18 validated. Missing automated 1000-record test. |
| **Code quality** | **A** | Clean, well-validated, transaction-safe, locale-aware, enum-safe, good error reporting. |
| **Architecture** | **A** | Correct module separation, shared entities, separate port, constructor injection, proper config. |
| **Test coverage** | **A-** | 17 default tests covering all tools + edge cases. Protocol tests exist but gated. |
| **Overall** | **A** | Solid implementation with comprehensive test coverage and clean config. |

---

## 9. Second Review Pass — Supporting Components

**Scope:** `api-models`, root `pom.xml`, `docker-compose.yml`, `AI-USAGE.md`, `README.md`  
**Excludes:** backend and frontend (verified separately).

---

### `api-models` Module

| File | Assessment |
|------|-----------|
| `Task.java` | ✅ Clean JPA entity. Constants (`TITLE_MAX_LENGTH`, `DESCRIPTION_MAX_LENGTH`) used in `@Size` and `@Column`. Default constructor sets `status = TODO`. Convenience constructor handles null status. `@Enumerated(STRING)` for PostgreSQL compatibility. |
| `TaskStatus.java` | ✅ Simple enum: `TODO`, `IN_PROGRESS`, `DONE`. |
| `TaskRepository.java` | ✅ `JpaRepository<Task, Long>` with `countByStatus(TaskStatus)` derived query. `@Repository` annotated. |
| `pom.xml` | ✅ Packaging `jar`, correct dependencies (`spring-boot-starter-data-jpa`, `spring-boot-starter-validation`). |

⚠️ **Minor:** `@Size` message in `Task.java` line 20 says `"Title must not exceed 100 characters"` (hardcoded number) instead of referencing the constant. Same for description on line 24. Not a bug (constants control the actual limit), but the message could drift if constants change.

---

### Root `pom.xml`

| Item | Value | Assessment |
|------|-------|-----------|
| Parent | `spring-boot-starter-parent:3.4.3` | ✅ Current |
| Modules | `api-models`, `frontend`, `backend`, `mcp-server` | ✅ Correct build order (api-models first) |
| `java.version` | `17` | ✅ Matches spec requirement |
| `spring-ai.version` | `1.1.0-M2` | ⚠️ Milestone release — production projects should pin to GA when available |
| `postgresql.version` | `42.2.27` | ⚠️ **Outdated** — latest is 42.7.x. 42.2.x is in maintenance-only mode. No security CVEs blocking, but worth upgrading. |

---

### `docker-compose.yml`

| Item | Assessment |
|------|-----------|
| Image | `postgres:16` | ✅ Good — explicit major version, no `latest` |
| Port | `${POSTGRES_PORT:-5436}:5432` | ✅ Env-configurable with sensible default |
| Credentials | Env-configurable with defaults matching `application.properties` | ✅ |
| Volume | `pgdata` named volume for persistence | ✅ |

✅ Clean and correct. No issues found.

---

### `AI-USAGE.md`

| Item | Assessment |
|------|-----------|
| Workflow documentation | ✅ Detailed multi-phase SDLC with tool selection rationale |
| MCP workflow section | ✅ 6-step log with human review documented per step |
| Critical reflection | ✅ Honest about challenges (context handoff, code style, validation gaps) |

⚠️ **Stale test count (line 117):** Says `"MCP server: 6/6 unit tests passed"` — should now be **13/13 unit tests** (8 original + 5 added).  
⚠️ **Stale annotation note (line 105):** Says `"Fixed incorrect annotations (@McpTool → @Tool for Spring AI 1.0.0 GA)"` — current code uses `@McpTool` from `org.springaicommunity.mcp.annotation`, not the Spring AI core `@Tool`. The note is confusing as-is.

---

### `README.md` — MCP Section

| Item | Assessment |
|------|-----------|
| MCP Server tools table | ✅ All 4 tools listed with correct descriptions |
| Running instructions | ✅ `java -jar mcp-server/target/mcp-server-0.0.1-SNAPSHOT.jar` |
| SSE endpoint | ✅ `http://localhost:8081/sse` |
| Example AI prompt | ✅ Present |

✅ MCP documentation in README is accurate and complete.

---

### Summary of Second Pass Findings

| # | Finding | Severity | Fix? |
|---|---------|----------|------|
| 1 | `postgresql.version=42.2.27` is outdated (latest 42.7.x) | ⚠️ Low | Optional — no CVE, but maintenance-only branch |
| 2 | `AI-USAGE.md` says 6/6 MCP tests — should be 13/13 | ⚠️ Low | Update line 117 |
| 3 | `AI-USAGE.md` annotation note is confusing | ⚠️ Low | Clarify or remove line 105 |
| 4 | `@Size` message strings hardcode numbers | Low | Cosmetic — doesn't affect behavior |
| 5 | `spring-ai.version=1.1.0-M2` is a milestone | Low | Pin to GA when released |
