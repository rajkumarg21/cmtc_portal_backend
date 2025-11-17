package com.cmtc.cms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleCorsFilter implements Filter {

    // Reads multiple origins separated by commas from application.properties
    @Value("${spring.web.cors.allowed-origins}")
    private String allowedOriginsConfig;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // Split comma-separated origins into a List
        List<String> allowedOrigins = Arrays.asList(allowedOriginsConfig.split(","));

        String requestOrigin = request.getHeader("Origin");

        // Check if the Origin header matches one of the allowed origins
        if (requestOrigin != null && allowedOrigins.stream().anyMatch(o -> o.trim().equalsIgnoreCase(requestOrigin))) {
            response.setHeader("Access-Control-Allow-Origin", requestOrigin);
        }

        // Always include Vary header to handle multiple origins correctly
        response.setHeader("Vary", "Origin");

        // Define standard CORS headers
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader(
                "Access-Control-Allow-Headers",
                "x-requested-with, authorization, Content-Type, Authorization, credential, X-XSRF-TOKEN"
        );
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Handle preflight (OPTIONS) requests immediately
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization required
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
