package br.com.senai.controller;

import br.com.senai.model.DTO.AuthResponseDTO;
import br.com.senai.model.DTO.LoginDTO;
import br.com.senai.model.DTO.SupabaseAuthResponseDTO;
import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.model.DTO.UserResponseDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.service.AuthService;
import br.com.senai.service.SupabaseAuthService;
import br.com.senai.util.JWTBlacklist;
import jakarta.validation.Valid;
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
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        SupabaseAuthResponseDTO supabaseResponse = supabaseAuthService.signIn(dto.getEmail(), dto.getPassword());
        UserEntity userEntity = authService.findBySupabaseUserId(supabaseResponse.getUser().getId());

        return ResponseEntity.ok(
                AuthResponseDTO.of(
                        supabaseResponse.getAccessToken(),
                        UserResponseDTO.fromEntity(userEntity)
                )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserDTO userDTO) {
        SupabaseUserDTO supabaseUserDTO = supabaseAuthService.signUp(
                userDTO.getEmail(),
                userDTO.getPassword(),
                authService.buildUserMetadata(userDTO)
        );

        UserEntity newUser = authService.register(userDTO, supabaseUserDTO.getId());
        return ResponseEntity.ok(UserResponseDTO.fromEntity(newUser));
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
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token inválido");
        }

        String token = tokenHeader.substring(7);
        SupabaseUserDTO supabaseUser = supabaseAuthService.validateToken(token);
        UserEntity userEntity = authService.findBySupabaseUserId(supabaseUser.getId());
        return ResponseEntity.ok(UserResponseDTO.fromEntity(userEntity));
    }
}
