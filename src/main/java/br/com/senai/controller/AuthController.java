package br.com.senai.controller;

import br.com.senai.model.DTO.LoginDTO;
import br.com.senai.model.DTO.SupabaseAuthResponseDTO;
import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.service.AuthService;
import br.com.senai.service.SupabaseAuthService;
import br.com.senai.util.JWTBlacklist;
import br.com.senai.util.JWTUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SupabaseAuthService supabaseAuthService;
    private final JWTBlacklist jwtBlacklist;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        try {
            // 1. Login no Supabase
            SupabaseAuthResponseDTO supabaseResponse = supabaseAuthService.signIn(dto.getEmail(), dto.getPassword());
            // 2. Verificar se usuário existe no banco local
            UserEntity userEntity = authService.findBySupabaseUserId(supabaseResponse.getUser().getId());
            if (userEntity == null) {
                return ResponseEntity.badRequest()
                        .body("Usuário não encontrado no sistema. Faça o cadastro primeiro.");
            }
            // 3. Retornar token do Supabase
            Map<String, Object> response = new HashMap<>();
            response.put("access_token", supabaseResponse.getAccessToken());
            response.put("user", userEntity);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        try {
            // 1. Registrar no Supabase
            Map<String, Object> userMetadata = new HashMap<>();
            userMetadata.put("name", userDTO.getName());
            userMetadata.put("phone", userDTO.getPhoneNumber());

            SupabaseUserDTO supabaseUserDTO = supabaseAuthService.signUp(
                    userDTO.getEmail(),
                    userDTO.getPassword(),
                    userMetadata
            );

            // 2. Registrar no banco local com o ID do Supabase
            UserEntity newUser = authService.register(userDTO, supabaseUserDTO.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuário criado com sucesso");
            response.put("user", newUser);
            response.put("supabase_user_id", supabaseUserDTO.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            jwtBlacklist.addToken(token);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String tokenHeader) {
        try {
            if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
                String token = tokenHeader.substring(7);
                var supabaseUser = supabaseAuthService.validateToken(token);

                UserEntity userEntity = authService.findBySupabaseUserId(supabaseUser.getId());

                if (userEntity != null) {
                    return ResponseEntity.ok(userEntity);
                }
            }
            return ResponseEntity.badRequest().body("Token inválido");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Token inválido: " + e.getMessage());
        }
    }
}
