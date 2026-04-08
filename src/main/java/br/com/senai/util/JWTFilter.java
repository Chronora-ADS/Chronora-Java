package br.com.senai.util;

import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.UserRepository;
import br.com.senai.service.SupabaseAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JWTFilter extends OncePerRequestFilter {
    private final SupabaseAuthService supabaseAuthService;
    private final UserRepository userRepository;
    private final JWTBlacklist jwtBlacklist;

    public JWTFilter(
            SupabaseAuthService supabaseAuthService,
            UserRepository userRepository,
            JWTBlacklist jwtBlacklist
    ) {
        this.supabaseAuthService = supabaseAuthService;
        this.userRepository = userRepository;
        this.jwtBlacklist = jwtBlacklist;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/auth/") || path.equals("/health") || path.equals("/healthz")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getToken(request);
        if (token == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalido");
            return;
        }

        if (jwtBlacklist.contains(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expirado");
            return;
        }

        try {
            SupabaseUserDTO supabaseUserDTO = supabaseAuthService.validateToken(token);
            Optional<UserEntity> userOptional = userRepository.findBySupabaseUserId(supabaseUserDTO.getId());

            if (userOptional.isPresent()) {
                UserEntity userEntity = userOptional.get();
                List<SimpleGrantedAuthority> authorities = userEntity.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userEntity.getEmail(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuario nao encontrado no sistema");
                return;
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalido: " + e.getMessage());
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
