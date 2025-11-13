package br.com.senai.controller;

import br.com.senai.model.DTO.SupabaseUserDTO;
import br.com.senai.model.DTO.UserDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.service.AuthService;
import br.com.senai.service.SupabaseAuthService;
import br.com.senai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SupabaseAuthService supabaseAuthService;
    private final AuthService authService;

    @GetMapping("/get/{id}")
    public ResponseEntity<UserEntity> getById(@PathVariable Long id) {
        UserEntity user = userService.getById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/get/document/{id}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Long id) {
        UserEntity user = userService.getById(id);
        return ResponseEntity.ok()
                .header("Content-Type", user.getDocumentEntity().getType())
                .header("Content-Disposition", "inline; filename=\"" + user.getDocumentEntity().getName() + "\"")
                .body(user.getDocumentEntity().getData());
    }

    @GetMapping("/get")
    public ResponseEntity<UserEntity> getLoggedUser(@RequestHeader("Authorization") String tokenHeader) {
        try {
            if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
                String token = tokenHeader.substring(7);

                // Valida o token no Supabase
                SupabaseUserDTO supabaseUser = supabaseAuthService.validateToken(token);

                // Busca o usuário no banco local pelo ID do Supabase usando UserService
                UserEntity userEntity = authService.findBySupabaseUserId(supabaseUser.getId());

                return ResponseEntity.ok(userEntity);
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}