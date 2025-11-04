package br.com.senai.controller;

import br.com.senai.model.DTO.LoginDTO;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.service.AuthService;
import br.com.senai.util.JWTBlacklist;
import br.com.senai.util.JWTUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JWTUtils jwtUtils;
    private final JWTBlacklist jwtBlacklist;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO dto) {
        UserEntity userEntity = authService.authenticate(dto);
        String token = jwtUtils.generateToken(userEntity);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@Valid @RequestBody UserDTO userDTO) {
        UserEntity newUser = authService.register(userDTO);
        return ResponseEntity.ok(newUser);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            jwtBlacklist.addToken(token);
        }
        return ResponseEntity.ok().build();
    }
}
