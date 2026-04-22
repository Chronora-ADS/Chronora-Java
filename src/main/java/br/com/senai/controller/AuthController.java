package br.com.senai.controller;

import br.com.senai.model.DTO.ForgotPasswordDTO;
import br.com.senai.model.DTO.LoginDTO;
import br.com.senai.model.DTO.RefreshTokenDTO;
import br.com.senai.model.DTO.ResetPasswordDTO;
import br.com.senai.model.DTO.SupabaseAuthResponseDTO;
import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.model.DTO.UserResponseDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.service.AuthService;
import br.com.senai.service.SupabaseAuthService;
import br.com.senai.util.JWTBlacklist;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
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

    public AuthController(
            AuthService authService,
            SupabaseAuthService supabaseAuthService,
            JWTBlacklist jwtBlacklist
    ) {
        this.authService = authService;
        this.supabaseAuthService = supabaseAuthService;
        this.jwtBlacklist = jwtBlacklist;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        SupabaseAuthResponseDTO session = supabaseAuthService.signIn(dto.getEmail(), dto.getPassword());
        return ResponseEntity.ok(toSessionResponse(session));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserDTO userDTO) {
        authService.validateUniqueEmailAndPhone(userDTO.getEmail(), userDTO.getPhoneNumber());

        SupabaseUserDTO supabaseUserDTO = supabaseAuthService.signUp(
                userDTO.getEmail(),
                userDTO.getPassword(),
                authService.buildUserMetadata(userDTO)
        );

        UserEntity newUser;
        try {
            newUser = authService.register(userDTO, supabaseUserDTO.getId());
        } catch (RuntimeException ex) {
            supabaseAuthService.deleteUser(supabaseUserDTO.getId());
            throw ex;
        }
        return ResponseEntity.ok(UserResponseDTO.fromEntity(newUser));
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
    public ResponseEntity<Void> resetPassword(
            @RequestHeader("Authorization") String tokenHeader,
            @Valid @RequestBody ResetPasswordDTO body
    ) {
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
        UserEntity userEntity = authService.findBySupabaseUserId(supabaseUser.getId());
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
