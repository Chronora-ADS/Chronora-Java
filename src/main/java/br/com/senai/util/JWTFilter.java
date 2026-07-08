package br.com.senai.util;

import br.com.senai.model.DTO.user.SupabaseUserDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.service.auth.AuthService;
import br.com.senai.service.auth.SupabaseAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
@AllArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTFilter.class);
    private final AuthService authService;
    private final SupabaseAuthService supabaseAuthService;
    private final JWTBlacklist jwtBlacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/auth/")
                || path.equals("/health")
                || path.equals("/healthz")
                || path.equals("/payment/webhook")
                || path.equals("/payment/config")
                || path.startsWith("/monitoring/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getToken(request);
        if (token == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido.");
            return;
        }

        if (jwtBlacklist.contains(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expirado.");
            return;
        }

        try {
            SupabaseUserDTO supabaseUserDTO = supabaseAuthService.validateToken(token);
            UserEntity userEntity = authService.resolveUserForSupabaseUser(supabaseUserDTO);
            // Injeta userId no MDC para rastreabilidade em toda a request
            MDC.put("userId", String.valueOf(userEntity.getId()));
            List<SimpleGrantedAuthority> authorities = userEntity.getRoles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userEntity.getEmail(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            logger.warn("Falha de autenticação | path={} | reason={}", path, e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.replace("Bearer ", "");
        }
        return null;
    }
}
