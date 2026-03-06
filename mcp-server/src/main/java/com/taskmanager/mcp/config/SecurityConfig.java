package com.taskmanager.mcp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SecurityConfig implements Filter {

    @Value("${mcp.server.api-key:test-api-key}")
    private String expectedApiKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Only protect the MCP endpoints (like /sse)
        if (req.getRequestURI().startsWith("/sse")) {
            String header = req.getHeader("Authorization");
            String apiKeyHeader = req.getHeader("X-API-Key");

            String providedKey = null;
            if (header != null && header.startsWith("Bearer ")) {
                providedKey = header.substring(7);
            } else if (apiKeyHeader != null) {
                providedKey = apiKeyHeader;
            }

            if (providedKey == null || !expectedApiKey.equals(providedKey)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("Unauthorized: Invalid or missing API Key");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
