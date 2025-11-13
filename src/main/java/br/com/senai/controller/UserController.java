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

    @PutMapping("/put/buy-chronos")
    public ResponseEntity<UserEntity> buyChronos(@RequestHeader("Authorization") String tokenHeader, @RequestHeader("Chronos") Integer chronos) {
        return ResponseEntity.ok(userService.buyChronos(tokenHeader, chronos));
    }

    @PutMapping("/put/sell-chronos")
    public ResponseEntity<UserEntity> sellChronos(@RequestHeader("Authorization") String tokenHeader, @RequestHeader("Chronos") Integer chronos) {
        return ResponseEntity.ok(userService.sellChronos(tokenHeader, chronos));
    }

    @GetMapping("/get")
    public ResponseEntity<UserEntity> getLoggedUser(@RequestHeader("Authorization") String tokenHeader) {
        return ResponseEntity.ok(userService.getLoggedUser(tokenHeader));
    }
}