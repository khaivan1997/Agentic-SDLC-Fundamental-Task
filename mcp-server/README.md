# Task Manager MCP Server

This module provides a Model Context Protocol (MCP) server for the Task Manager application, built using the Spring AI MCP framework. It allows AI agents and LLM clients to securely interact with the Task Management database.

## Features & Protocol Support
- Protocol Version: `2025-06-18`
- Transport: HTTP Server-Sent Events (SSE)
- Framework: `spring-ai-starter-mcp-server-webmvc`

## Available Tools

| Tool | Description |
|------|-------------|
| `mcp-help` | Returns a list of all available MCP tools in this server along with their descriptions and usage. |
| `mcp-schema-tasks` | Provides the JSON Schema definition for the Tasks table. Useful for AI agents to understand the required data structure for bulk inserts. |
| `mcp-tasks` | Performs a bulk insert of new Tasks. Accepts a JSON array of task objects. Includes partial failure handling and returns a summary of inserted vs. rejected tasks. *(Limit: 10,000 tasks per batch)* |
| `mcp-tasks-summary` | Returns an aggregate count of all tasks in the database, grouped by their current status (e.g., TODO, IN_PROGRESS, DONE). |

## Security
MCP transport endpoints are protected by an API Key filter:
- `/sse`
- `/mcp/message` (default SSE message endpoint)

All requests must include the secret key to be processed.

**Authentication Headers (pick one):**
- `Authorization: Bearer <your-api-key>`
- `X-API-Key: <your-api-key>`

The expected key is configured via the `mcp.server.api-key` property in `application.properties` (backed by environment variable `MCP_SERVER_API_KEY`).
In production, `MCP_SERVER_API_KEY` must be provided explicitly.
For safety, startup rejects the insecure value `test-api-key` unless running with `test`, `local`, or `dev` profile.

## Configuration Examples

### Claude Desktop Configuration
If you want to use this MCP server with the Claude Desktop app, you can configure it via your `claude_mcp_config.json`. 

*Note: Claude Desktop primarily supports STDIO or SSE via specific client configs. If using SSE directly, you need a proxy or extension that natively supports injecting auth headers.*

```json
{
  "mcpServers": {
    "taskmanager": {
      "command": "node",
      "args": ["sse-client-proxy.js", "http://localhost:8081/sse", "test-api-key"]
    }
  }
}
```

## Running the Server
The MCP server can be run as a standalone Spring Boot application, connecting to the shared PostgreSQL database.

### Local profile (dev defaults)
This project includes `application-local.properties` for local development:
- DB: `jdbc:postgresql://localhost:5436/taskdb`
- user/password: `taskuser` / `taskpass`
- API key: `test-api-key`

```bash
cd mcp-server
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Production-style run (explicit secrets required)
`application.properties` requires these environment variables:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `MCP_SERVER_API_KEY`

### Health endpoint
Actuator health endpoint is enabled:
- `GET /actuator/health`

### Container image
Build and run:
```bash
cd mcp-server
mvn -DskipTests package
docker build -t task-manager-mcp .
docker run --rm -p 8081:8081 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5436/taskdb \
  -e DB_USERNAME=taskuser \
  -e DB_PASSWORD=taskpass \
  -e MCP_SERVER_API_KEY=change-me \
  task-manager-mcp
```
