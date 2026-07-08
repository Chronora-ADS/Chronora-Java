package br.com.senai.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Injeta requestId, userId, method e path no MDC antes de cada requisição.
 * Garante rastreabilidade completa em todos os logs da request.
 * Executa APÓS o JWTFilter (order=2) para capturar o userId autenticado.
 */
@Component
@Order(2)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(MdcLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        MDC.put("requestId", requestId);
        MDC.put("method", request.getMethod());
        MDC.put("path", request.getRequestURI());
        response.setHeader("X-Request-ID", requestId);

        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            // Tenta capturar userId do contexto de segurança (disponível após JWTFilter)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                MDC.put("userId", auth.getName());
            }

            long elapsed = System.currentTimeMillis() - start;
            int status = response.getStatus();
            String level = status >= 500 ? "ERROR" : status >= 400 ? "WARN" : "INFO";

            if ("ERROR".equals(level)) {
                logger.error("HTTP {} {} → {} ({}ms)", request.getMethod(),
                        request.getRequestURI(), status, elapsed);
            } else if ("WARN".equals(level)) {
                logger.warn("HTTP {} {} → {} ({}ms)", request.getMethod(),
                        request.getRequestURI(), status, elapsed);
            } else {
                logger.info("HTTP {} {} → {} ({}ms)", request.getMethod(),
                        request.getRequestURI(), status, elapsed);
            }

            MDC.clear();
        }
    }
}
