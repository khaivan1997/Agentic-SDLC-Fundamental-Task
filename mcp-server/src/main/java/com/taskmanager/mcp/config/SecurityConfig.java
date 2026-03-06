package com.taskmanager.mcp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Arrays;
import java.util.Objects;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Configuration
public class SecurityConfig implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private static final String INSECURE_TEST_KEY = "test-api-key";

    @Value("${mcp.server.api-key}")
    private String expectedApiKey;

    @Value("${spring.ai.mcp.server.sse-endpoint:/sse}")
    private String sseEndpoint;

    @Value("${spring.ai.mcp.server.sse-message-endpoint:/mcp/message}")
    private String sseMessageEndpoint;

    private final Environment environment;

    public SecurityConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    void validateApiKeyConfiguration() {
        boolean testOrLocalProfile = Arrays.stream(environment.getActiveProfiles())
                .map(String::toLowerCase)
                .anyMatch(p -> p.equals("test") || p.equals("local") || p.equals("dev"));

        if (!testOrLocalProfile && INSECURE_TEST_KEY.equals(expectedApiKey)) {
            throw new IllegalStateException(
                    "Refusing to start with insecure API key 'test-api-key' outside test/local/dev profile");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Protect MCP transport endpoints (SSE handshake + message endpoint)
        if (isProtectedMcpEndpoint(req.getRequestURI())) {
            String header = req.getHeader("Authorization");
            String apiKeyHeader = req.getHeader("X-API-Key");

            String providedKey = null;
            if (header != null && header.startsWith("Bearer ")) {
                providedKey = header.substring(7);
            } else if (apiKeyHeader != null) {
                providedKey = apiKeyHeader;
            }

            if (providedKey == null || !expectedApiKey.equals(providedKey)) {
                log.warn("Unauthorized MCP access attempt path={} remote={}", req.getRequestURI(), req.getRemoteAddr());
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("Unauthorized: Invalid or missing API Key");
                return;
            }

            MDC.put("mcp.client", buildClientContext(req, providedKey));
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("mcp.client");
        }
    }

    private String buildClientContext(HttpServletRequest req, String apiKey) {
        String keyHash = shortSha256(apiKey);
        String remote = Objects.toString(req.getRemoteAddr(), "unknown");
        return remote + "|keyHash=" + keyHash;
    }

    private String shortSha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 10);
        } catch (NoSuchAlgorithmException ex) {
            return "hash-unavailable";
        }
    }

    private boolean isProtectedMcpEndpoint(String requestUri) {
        return requestUri.startsWith(sseEndpoint) || requestUri.startsWith(sseMessageEndpoint);
    }
}
