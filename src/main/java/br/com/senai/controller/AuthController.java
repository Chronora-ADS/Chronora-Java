package br.com.senai.controller;

import br.com.senai.model.DTO.user.ForgotPasswordDTO;
import br.com.senai.model.DTO.user.LoginDTO;
import br.com.senai.model.DTO.user.RefreshTokenDTO;
import br.com.senai.model.DTO.user.ResetPasswordDTO;
import br.com.senai.model.DTO.user.SupabaseAuthResponseDTO;
import br.com.senai.model.DTO.user.SupabaseUserDTO;
import br.com.senai.model.DTO.user.UserDTO;
import br.com.senai.model.DTO.user.UserResponseDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.service.auth.AuthService;
import br.com.senai.service.auth.SupabaseAuthService;
import br.com.senai.util.JWTBlacklist;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final SupabaseAuthService supabaseAuthService;
    private final JWTBlacklist jwtBlacklist;

    public AuthController(AuthService authService, SupabaseAuthService supabaseAuthService, JWTBlacklist jwtBlacklist) {
        this.authService = authService;
        this.supabaseAuthService = supabaseAuthService;
        this.jwtBlacklist = jwtBlacklist;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        SupabaseAuthResponseDTO session = supabaseAuthService.signIn(dto.getEmail(), dto.getPassword());
        authService.resolveUserForSupabaseUser(session.getUser());
        return ResponseEntity.ok(toSessionResponse(session));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserDTO userDTO) {
        authService.validateRegistrationAvailable(userDTO);
        SupabaseUserDTO supabaseUserDTO = supabaseAuthService.signUp(userDTO.getEmail(), userDTO.getPassword(), authService.buildUserMetadata(userDTO));

        UserEntity newUser;
        try {
            newUser = authService.register(userDTO, supabaseUserDTO.getId());
        } catch (RuntimeException ex) {
            supabaseAuthService.deleteUser(supabaseUserDTO.getId());
            throw ex;
        }
        return ResponseEntity.ok(UserResponseDTO.fromEntity(newUser));
    }

    @GetMapping(value = "/email-confirmed", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> emailConfirmed() {
        String html = """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Chronora - E-mail confirmado</title>
                  <style>
                    body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; background: #f5f5f5; }
                    .card { background: white; border-radius: 16px; padding: 40px; text-align: center; max-width: 400px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
                    .icon { font-size: 64px; margin-bottom: 16px; }
                    h1 { color: #C8A100; margin: 0 0 12px; }
                    p { color: #555; line-height: 1.5; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <div class="icon">✅</div>
                    <h1>E-mail confirmado!</h1>
                    <p>Sua conta foi verificada com sucesso.<br>Você já pode fazer login no app <strong>Chronora</strong>.</p>
                  </div>
                </body>
                </html>
                """;
        return ResponseEntity.ok(html);
    }

    @PostMapping("/resend-confirmation")
    public ResponseEntity<Void> resendConfirmation(@RequestBody Map<String, String> body) {
        supabaseAuthService.resendConfirmation(body.get("email"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordDTO body) {
        supabaseAuthService.sendPasswordRecovery(body.getEmail(), body.getRedirectTo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@Valid @RequestBody RefreshTokenDTO body) {
        SupabaseAuthResponseDTO session = supabaseAuthService.refreshSession(body.getRefreshToken());
        return ResponseEntity.ok(toSessionResponse(session));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestHeader("Authorization") String tokenHeader, @Valid @RequestBody ResetPasswordDTO body) {
        supabaseAuthService.resetPassword(extractBearerToken(tokenHeader), body.getNewPassword());
        return ResponseEntity.ok().build();
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
    public ResponseEntity<UserResponseDTO> validateToken(@RequestHeader("Authorization") String tokenHeader) {
        String token = extractBearerToken(tokenHeader);
        SupabaseUserDTO supabaseUser = supabaseAuthService.validateToken(token);
        UserEntity userEntity = authService.resolveUserForSupabaseUser(supabaseUser);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(userEntity));
    }

    private String extractBearerToken(String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token invalido");
        }
        return tokenHeader.substring(7);
    }

    private Map<String, Object> toSessionResponse(SupabaseAuthResponseDTO session) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("access_token", session.getAccessToken());

        if (session.getRefreshToken() != null) {
            response.put("refresh_token", session.getRefreshToken());
        }
        if (session.getExpiresIn() != null) {
            response.put("expires_in", session.getExpiresIn());
        }

        return response;
    }
}
