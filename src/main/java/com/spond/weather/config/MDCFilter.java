package com.spond.weather.config;

import jakarta.servlet.*;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class MDCFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            // Generate a UUID and put it in the MDC
            String requestId = UUID.randomUUID().toString();
            ThreadContext.put("requestId", requestId);

            // Proceed with the next filter in the chain
            chain.doFilter(request, response);
        } finally {
            // Clear the MDC after the request is processed
            ThreadContext.clearAll();
        }
    }

    @Override
    public void destroy() {
    }
}
