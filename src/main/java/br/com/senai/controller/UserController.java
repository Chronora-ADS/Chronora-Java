package br.com.senai.controller;

import br.com.senai.model.entity.UserEntity;
import br.com.senai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
        return ResponseEntity.ok(userService.getLoggedUser(tokenHeader));
    }
}