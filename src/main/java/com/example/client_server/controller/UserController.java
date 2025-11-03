package com.example.client_server.controller;

import com.example.client_server.model.DTO.UserDTO;
import com.example.client_server.model.entity.UserEntity;
import com.example.client_server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

//    @PostMapping(path = "/post", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<UserEntity> create(@RequestBody UserDTO userDTO) {
//        UserEntity userEntity = userService.create(userDTO);
//        return ResponseEntity.ok(userEntity);
//    }

    @GetMapping("/get/{id}")
    public ResponseEntity<UserEntity> getById(@PathVariable Long id) {
        return userService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/get/document/{id}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Long id) {
        return userService.getById(id)
                .map(user -> ResponseEntity.ok()
                        .header("Content-Type", user.getDocumentEntity().getType())
                        .header("Content-Disposition", "inline; filename=\"" + user.getDocumentEntity().getName() + "\"")
                        .body(user.getDocumentEntity().getData()))
                .orElse(ResponseEntity.notFound().build());
    }
}