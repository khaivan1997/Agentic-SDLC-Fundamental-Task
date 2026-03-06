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
The MCP `/sse` endpoint is protected by an API Key filter. All requests must include the secret key to be processed.

**Authentication Headers (pick one):**
- `Authorization: Bearer <your-api-key>`
- `X-API-Key: <your-api-key>`

The expected key is configured via the `mcp.server.api-key` property in `application.properties` (or environment variable `MCP_SERVER_API_KEY`). The default for dev/test is `test-api-key`.

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

```bash
cd mcp-server
mvn spring-boot:run
```
