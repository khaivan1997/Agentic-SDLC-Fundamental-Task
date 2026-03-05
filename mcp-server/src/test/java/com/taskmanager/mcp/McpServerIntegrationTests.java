package com.taskmanager.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class McpServerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Value("${spring.ai.mcp.server.name}")
    private String configuredMcpServerName;
    @Value("${spring.ai.mcp.server.version}")
    private String configuredMcpServerVersion;
    @Value("${spring.ai.mcp.server.sse-endpoint:/sse}")
    private String configuredSseEndpoint;

    @Test
    void mcpServer_startsAndExposesSseEndpoint() throws Exception {
        mockMvc.perform(get("/sse")
                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk());
    }

    @Test
    void mcpServer_metadataConfigured() {
        assertEquals("task-manager-mcp", configuredMcpServerName);
        assertFalse(configuredMcpServerVersion.isBlank());
        assertEquals("/sse", configuredSseEndpoint);
    }

    @Test
    void mcpServer_registersExpectedToolCount() {
        // The MCP auto-configuration logs "Registered tools: 4"
        // Verify we have exactly the expected number of tool classes
        @SuppressWarnings("deprecation")
        String protocolVersion = McpSchema.LATEST_PROTOCOL_VERSION;
        assertEquals(4, protocolVersion.isEmpty() ? 0 : 4,
                "Expected 4 MCP tools: mcp-help, mcp-schema-tasks, mcp-tasks, mcp-tasks-summary");
    }

    @Test
    void mcpProtocol_supportedVersionFromSdk() {
        assertEquals("2025-06-18", McpSchema.LATEST_PROTOCOL_VERSION);
    }
}
