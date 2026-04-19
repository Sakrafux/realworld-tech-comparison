package com.sakrafux.realworld.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * A servlet filter that intercepts incoming HTTP requests to measure their execution time
 * and log relevant request details (method, path, IP, User-Agent). 
 * It also generates a unique trace ID for each request to aid in distributed tracing and log aggregation.
 */
@Component
@Slf4j
public class RequestTimingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // Put the generated traceId into the Mapped Diagnostic Context (MDC).
        // This allows logging frameworks (like Logback/SLF4J) to automatically include 
        // this traceId in all log statements executed during the context of this thread's request.
        MDC.put(TRACE_ID, traceId);
        
        long startTime = System.currentTimeMillis();
        String path = request.getRequestURI();
        String method = request.getMethod();

        log.info("Incoming Request: {} {} | IP: {} | UA: {}", method, path, ip, userAgent);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            
            log.info("Finished Request: {} {} | Status: {} | Time: {}ms", method, path, status, duration);
            
            // Clean up the MDC at the end of the request.
            // Since servlet containers reuse threads from a thread pool, failing to clear the MDC 
            // would cause the traceId to leak into subsequent, unrelated requests handled by the same thread.
            MDC.clear();
        }
    }
}
