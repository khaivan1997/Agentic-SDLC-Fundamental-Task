package com.taskmanager.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfSystemProperty(named = "mcp.handshake.test", matches = "true")
class McpProtocolHandshakeIT {

    @LocalServerPort
    private int serverPort;

    @Value("${spring.ai.mcp.server.sse-endpoint:/sse}")
    private String sseEndpoint;

    private McpSyncClient createClient() {
        HttpClientSseClientTransport transport = HttpClientSseClientTransport
                .builder("http://localhost:" + serverPort)
                .sseEndpoint(sseEndpoint)
                .build();
        return McpClient.sync(transport).build();
    }

    @Test
    void initializeNegotiatesSdkProtocolVersion() {
        McpSyncClient client = createClient();
        try {
            McpSchema.InitializeResult result = client.initialize();
            assertEquals(McpSchema.LATEST_PROTOCOL_VERSION, result.protocolVersion());
        } finally {
            client.close();
        }
    }

    @Test
    void mcpTools_areDiscoverableAndCallableOverProtocol() {
        McpSyncClient client = createClient();
        try {
            client.initialize();

            McpSchema.ListToolsResult tools = client.listTools();
            Set<String> names = tools.tools().stream().map(McpSchema.Tool::name).collect(Collectors.toSet());
            assertTrue(names.contains("mcp-help"));
            assertTrue(names.contains("mcp-schema-tasks"));
            assertTrue(names.contains("mcp-tasks"));
            assertTrue(names.contains("mcp-tasks-summary"));

            McpSchema.CallToolResult schemaResult = client.callTool(
                    new McpSchema.CallToolRequest("mcp-schema-tasks", Map.of()));
            assertTrue(Boolean.FALSE.equals(schemaResult.isError()));

            McpSchema.CallToolResult summaryResult = client.callTool(
                    new McpSchema.CallToolRequest("mcp-tasks-summary", Map.of()));
            assertTrue(Boolean.FALSE.equals(summaryResult.isError()));
        } finally {
            client.close();
        }
    }

    @Test
    void mcpTasks_bulkInsertAndSummaryOverProtocol() {
        McpSyncClient client = createClient();
        try {
            client.initialize();

            var tasks = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < 1000; i++) {
                tasks.add(Map.of(
                        "title", "Proto Task " + i,
                        "description", "Generated via MCP protocol " + i,
                        "status", (i % 3 == 0 ? "TODO" : (i % 3 == 1 ? "IN_PROGRESS" : "DONE")),
                        "dueDate", LocalDate.of(2026, 1, 1).plusDays(i % 20).toString()));
            }

            McpSchema.CallToolResult insertResult = client.callTool(
                    new McpSchema.CallToolRequest("mcp-tasks", Map.of("tasks", tasks)));
            assertTrue(Boolean.FALSE.equals(insertResult.isError()));
            String insertText = ((McpSchema.TextContent) insertResult.content().get(0)).text();
            assertTrue(insertText.contains("\"inserted\":1000") || insertText.contains("\"inserted\": 1000"));

            McpSchema.CallToolResult summaryResult = client.callTool(
                    new McpSchema.CallToolRequest("mcp-tasks-summary", Map.of()));
            assertTrue(Boolean.FALSE.equals(summaryResult.isError()));
            String summaryText = ((McpSchema.TextContent) summaryResult.content().get(0)).text();
            assertTrue(summaryText.contains("\"total\":1000") || summaryText.contains("\"total\": 1000"));
        } finally {
            client.close();
        }
    }
}
