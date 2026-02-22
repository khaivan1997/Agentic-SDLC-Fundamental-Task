package com.taskmanager.mcp;

import com.taskmanager.mcp.dto.TaskInput;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void mcpModelClassesAreLoadable() {
        TaskInput sample = new TaskInput();
        sample.setTitle("sample");
        assertEquals("sample", sample.getTitle());
        assertNotNull(sample);
    }

    @Test
    void mcpProtocol_supportedVersionFromSdk() {
        assertEquals("2025-06-18", McpSchema.LATEST_PROTOCOL_VERSION);
    }
}
