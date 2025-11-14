package com.cmtc.cms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cmtc.cms.security.JwtUtil;
import com.cmtc.cms.serviceImpl.UserDetailsServiceImpl;

import java.io.IOException;
import java.util.stream.Collectors; // Added for logging authorities

import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class); // Logger instance

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.info("Processing request for path: {}", path); // Log incoming request path

//
//        // --- Forward to index.html only if it's a frontend route (e.g., React app route)
//        if (!isBackendCall(path) && !isSwaggerOrStaticResource(path)) {
//            request.getRequestDispatcher("/index.html").forward(request, response);
//            logger.info("completed frontend response public path: {}", path);
//
//            return;
//        }


        // 👇 Skip JWT processing for login and other public endpoints
        if (path.startsWith("/api/auth/login") ||
        	    path.startsWith("/api/auth/forgot-password") ||
        	    path.startsWith("/api/auth/reset-password") ||
        	    path.startsWith("/api/public/") ||
        	    path.startsWith("/api/news/public") ||
        	    path.startsWith("/api/circulars/public") || // Added for completeness
                path.startsWith("/api/static-pages/public") || // Added for completeness
                path.startsWith("/api/gallery/public") || // Added for completeness
                path.startsWith("/api/books/public") || // Added for completeness
                path.startsWith("/api/authors/public") || // Added for completeness
                path.startsWith("/api/contact/submit") || // Added for completeness
                path.startsWith("/api/feedback/submit") || // Added for completeness
                path.startsWith("/api/rti/submit") || // Added for completeness
                path.startsWith("/api/rti/public/status") || // Added for completeness
        	    path.startsWith("/swagger-ui") ||
        	    path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator") ||
        	    path.startsWith("/files") ||
        	    path.equals("/") || path.equals("/index.html")) {

            logger.info("Skipping JWT authentication for public path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // JWT logic starts here
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        logger.info("Authorization Header: {}", authHeader != null ? "Present" : "Missing");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.info("Extracted Token: {}", token.substring(0, Math.min(token.length(), 30)) + "..."); // Log partial token
            username = jwtUtil.extractUsername(token);
            logger.info("Extracted Username from token: {}", username);
        } else {
            logger.warn("Authorization header is missing or does not start with Bearer for path: {}", path);
        }

        // Check if username is extracted and no authentication exists in context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.info("Attempting to authenticate user: {}", username);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Log authorities from UserDetails
            if (userDetails != null) {
                String authorities = userDetails.getAuthorities().stream()
                                    .map(Object::toString)
                                    .collect(Collectors.joining(", "));
                logger.info("User {} has authorities: {}", username, authorities);
            } else {
                logger.warn("UserDetails not found for username: {}", username);
            }


            if (jwtUtil.validateToken(token, userDetails)) {
                logger.info("JWT token validated successfully for user: {}", username);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("SecurityContextHolder populated for user: {}", username);
            } else {
                logger.warn("JWT token validation failed for user: {}", username);
            }
        } else if (username == null) {
            logger.warn("Username could not be extracted from token for path: {}", path);
        } else {
            logger.info("User already authenticated in SecurityContext for path: {}", path);
        }

        filterChain.doFilter(request, response);
    }


    private boolean isBackendCall(String path) {
        return path.startsWith("/api") || path.startsWith("/auth") || path.startsWith("/files") || path.contains(".");
    }

    private boolean isAllowedFrontendPath(String path) {
        return path.equals("/") || path.equals("/index.html");
    }

    private boolean isSwaggerOrStaticResource(String path) {
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/h2-console") || path.startsWith("/actuator");
    }

}

