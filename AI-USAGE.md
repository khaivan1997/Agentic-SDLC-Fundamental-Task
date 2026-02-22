# AI Tools Usage Documentation

This project ("Agentic SDLC Fundamental Task") was built using multiple AI models, orchestrated through **GitHub Copilot** as the agentic IDE platform. GitHub Copilot allowed seamless switching between different AI models depending on the task at hand.

## AI Tools Used

| Tool | Role |
| :--- | :--- |
| **GitHub Copilot** | IDE agent platform for navigating between AI models |
| **Google Gemini** | PDF extraction, document review |
| **Anthropic Claude** | Context/planning document writing, code review |
| **OpenAI GPT** | Code generation, debugging, bug fixing |

## Step-by-Step Workflow

### Phase 1: Requirements Extraction
- **Tool**: Google Gemini
- **Action**: Used Gemini to extract the contents of `request.pdf` (the original assignment) into a machine-readable `request.txt` file.
- **Output**: `context/request.txt`

### Phase 2: Architecture & Planning
- **Tool**: Anthropic Claude
- **Action**: Provided `request.txt` as input and instructed Claude to draft the high-level architecture, deployment strategy, and technology decisions.
- **Output**: `context/architecture.txt`

### Phase 3: Frontend & Backend Planning
- **Tool**: Anthropic Claude
- **Action**: Instructed Claude to break the architecture into component-level implementation plans for the frontend and backend, covering all functional requirements from `request.txt`.
- **Output**: `context/frontend.txt`, `context/backend.txt`

### Phase 4: Test Strategy (Acceptance Criteria)
- **Tool**: Anthropic Claude
- **Action**: Before any code was written, instructed Claude to analyze the functional requirements and define a comprehensive backend testing strategy with specific, measurable acceptance criteria.
- **Output**: `context/backend-test.txt`

### Phase 5: Planning Review
- **Tool**: Google Gemini
- **Action**: Used Gemini to review all planning documents (`architecture.txt`, `frontend.txt`, `backend.txt`, `backend-test.txt`) against `request.txt` to verify completeness and correctness before proceeding to implementation.
- **Output**: Review feedback was incorporated into the planning documents.

### Phase 6: Implementation
- **Tool**: OpenAI GPT
- **Action**: Using the established planning documents as blueprints, GPT generated the Spring Boot backend, React frontend, Maven multi-module configuration, and resolved build/compilation issues.
- **Output**: Complete codebase (backend + frontend)

### Phase 7: Code Review
- **Tool**: Anthropic Claude
- **Action**: Claude reviewed the generated code for correctness, code structure, and adherence to the planning documents.
- **Output**: Review feedback identifying issues and improvements.

### Phase 8: Bug Fixing
- **Tool**: OpenAI GPT
- **Action**: GPT addressed the issues identified during Claude's code review, fixing bugs, improving error handling, and refactoring where needed.
- **Output**: Updated, production-ready codebase.

### Phase 9: Verification & Test Results
- **Tool**: Anthropic Claude
- **Action**: After implementation, Claude generated the final test results document justifying what was tested against the acceptance criteria.
- **Output**: `context/test-result.txt`

## Critical Reflection on AI Usage

### What Worked Well
- **Multi-agent separation of concerns**: Using different AI tools for different SDLC phases prevented any single tool from losing context or drifting from requirements.
- **Planning-first approach**: Having Claude write detailed planning documents before GPT touched any code resulted in a more structured and predictable implementation.
- **Cross-tool review**: Using Gemini to review Claude's planning documents, and Claude to review GPT's code, introduced a natural "second opinion" that caught gaps early.

### Challenges & Limitations
- **Context handoff**: Each tool needed to be re-briefed with the relevant context documents when switching between phases. This required careful prompt engineering.
- **AI-generated code style**: Copilot occasionally produced code that worked but wasn't idiomatic. Claude's review step helped catch these issues.
- **Validation gaps**: AI tools sometimes assumed requirements were met without explicitly verifying edge cases. The test-driven planning approach (writing `backend-test.txt` before coding) helped mitigate this.

### Lessons Learned
- A structured, document-driven workflow with AI is more effective than ad-hoc prompting.
- AI tools complement each other when given clearly scoped responsibilities.
- Human oversight remains essential for architectural decisions and quality control.

## AI Workflow Log MCP

Each step below includes human review and decision-making. AI output was never accepted blindly — every artifact was reviewed, adjusted, or rejected by the developer before proceeding.

### 1) Requirement Intake
- Input source: `context/request-ext.pdf`
- Action: Used Gemini to extract PDF content to `context/request-ext.txt`.
- **Human review**: Developer verified extracted text against the original PDF, corrected formatting, and confirmed it as the MCP requirement baseline.

### 2) Requirement Analysis and Context Alignment
- Action: Asked Claude to analyze MCP requirements and align/update current context `.txt` files accordingly.
- Files updated: `context/mcp-architecture.txt`, `context/mcp-test.txt`.
- **Human review**: Developer reviewed AI-proposed scope, removed irrelevant items (e.g., Swagger for MCP), and ensured alignment with the original requirements before finalizing.

### 3) Architecture and Test Planning
- Action: Planned MCP architecture and test strategy and used Gemini to cross-review the plans.
- **Human review**: Developer evaluated module boundaries, decided on shared `api-models` approach, and refined test coverage expectations. Architecture decisions (e.g., shared DB, module layout) were human-driven.

### 4) Implementation
- Action: Asked Codex to adjust the codebase to match updated requirements and architecture.
- Main outcomes:
  - Added/used shared `api-models` module for shared entities/repository.
  - Updated backend and MCP configuration for shared PostgreSQL.
  - Implemented/refined MCP tools and module wiring in `mcp-server`.
- **Human review and fixes**: Developer manually applied several code quality improvements that AI missed:
  - Replaced hardcoded magic numbers with `Task.TITLE_MAX_LENGTH` / `DESCRIPTION_MAX_LENGTH` constants.
  - Externalized DB credentials and CORS origins to environment variables.
  - Fixed incorrect annotations (`@McpTool` → `@Tool` for Spring AI 1.0.0 GA).
  - Cleaned up error handling in frontend (`alert()` → inline error display, removed unused catch params).
  - Added `Locale.ROOT` to `toUpperCase()` for locale safety.
  - Resolved Maven dependency issues caused by corporate Artifactory.

### 5) Code Review
- Action: Asked Claude to perform code review.
- Focus: duplication, hardcoded config, redundancy, code cleanliness, and bug risks.
- **Human review**: Developer triaged review findings, decided which to fix immediately vs. defer, and applied targeted refactors. Also wrote unit tests for MCP tools based on review insights.

### 6) Build/Test Results
- Backend: 14/14 unit tests passed.
- MCP server: 6/6 unit tests passed (written by developer after code review).
- **Human verification**: Developer ran builds, inspected terminal output, confirmed test results, and resolved build blockers (corrupted Maven cache, annotation mismatches) manually.
- Evidence: `context/test-result.txt` with detailed per-module results.

## Continuous AI Review Policy

For every code change (feature, refactor, bug fix, or dependency upgrade), the team follows a repeated AI-assisted quality cycle:

1. Implement change.
2. Run AI code review.
3. Execute tests/build checks.
4. Collect feedback/findings.
5. Apply fixes.
6. Repeat review + test cycle until acceptance criteria are satisfied.

This cycle is mandatory for all meaningful changes to reduce regressions and keep architecture, code quality, and tests aligned with requirements.
