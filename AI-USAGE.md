# AI Tools Usage Documentation

This project ("Agentic SDLC Fundamental Task") was built using a strictly defined, iterative Agentic AI workflow. The AI agent was guided through the Software Development Life Cycle (SDLC) step-by-step to ensure precision and maintain control over the generated architecture and code.

## How the AI Agent was Utilized

The collaboration with the AI followed this structured process:

### 1. Requirements Understanding & Architecture Planning
- **Action**: Instructed the AI to strictly read and analyze `request.txt` to understand the core functional and technical requirements.
- **Output**: The AI agent drafted the `context/architecture.txt` file, deciding on a single WAR file deployment containing both the Spring Boot backend and the React Vite frontend using a Maven Multi-Module setup.

### 2. Component-Level Planning
- **Action**: Instructed the AI to divide the system and create dedicated plan documents for the frontend and backend implementations based on the initial architecture.
- **Output**: The AI created `context/backend.txt` (detailing the H2 database schema, entity structures, and REST API definitions) and `context/frontend.txt` (detailing the React component tree, state management, and design system).

### 3. Test-Driven Planning
- **Action**: Before any code was written, the AI was instructed to analyze the acceptance criteria from the functional requirements to formulate a comprehensive testing strategy.
- **Output**: The AI generated `context/backend-test.txt`, laying out specific Integration Testing parameters (e.g., maximum string lengths, null checks, and CORS definitions) mapped directly to the API endpoints.

### 4. Implementation
- **Action**: Using the established `architecture.txt`, `backend.txt`, and `frontend.txt` blueprints, the AI agent was tasked with generating the actual codebase.
- **Output**: The AI scaffolded the Spring Boot application and the React framework, implemented the cross-module Maven build hooks, and resolved compilation and proxy network issues autonomously.

### 5. Verification & Acceptance
- **Action**: Following the implementation, the AI agent evaluated the working software against the previously defined `backend-test.txt` acceptance criteria to justify and prove that the application fulfills the initial requirements.
- **Output**: The AI executed E2E tests using an autonomous browser subagent and generated `context/test-result.txt` to document exactly what was tested and validated.
