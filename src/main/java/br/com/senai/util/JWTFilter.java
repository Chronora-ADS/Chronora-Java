package br.com.senai.util;

import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.UserRepository;
import br.com.senai.service.SupabaseAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
//    private final JWTUtils jwtUtils;
    private final SupabaseAuthService supabaseAuthService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Lista de rotas públicas que NÃO precisam de token
        String path = request.getRequestURI();

        // Se for rota pública, pula a validação do token
        if (path.startsWith("/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // A partir daqui, aplica-se a validação do token apenas para rotas protegidas
        String token = getToken(request);
        if (token == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        }

        try {
            // Valida o token com Supabase
            SupabaseUserDTO supabaseUserDTO = supabaseAuthService.validateToken(token);

            // Busca usuário no banco local pelo ID do Supabase
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
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuário não encontrado no sistema");
                return;
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido: " + e.getMessage());
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
