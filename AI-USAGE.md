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
