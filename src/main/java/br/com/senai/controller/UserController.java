package br.com.senai.controller;

import br.com.senai.model.DTO.ServiceEditDTO;
import br.com.senai.model.DTO.UserEditDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

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

    @PutMapping("/put")
    public ResponseEntity<UserEntity> put(@RequestHeader("Authorization") String tokenHeader, @RequestBody UserEditDTO userEditDTO) {
        logger.info("Editando serviço: {}", userEditDTO);
        UserEntity saved = userService.put(userEditDTO, tokenHeader);
        logger.info("Serviço editado com sucesso: {}", saved.getId());
        return ResponseEntity.ok(saved);
    }
}