package br.com.senai.controller;

import br.com.senai.model.DTO.user.FcmTokenDTO;
import br.com.senai.model.DTO.user.UserEditDTO;
import br.com.senai.model.DTO.user.UserResponseDTO;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.service.user.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/put/buy-chronos")
    public ResponseEntity<UserResponseDTO> buyChronos(@RequestHeader("Authorization") String tokenHeader, @RequestHeader("Chronos") Integer chronos) {
        return ResponseEntity.ok(UserResponseDTO.fromEntity(userService.buyChronos(tokenHeader, chronos)));
    }

    @PutMapping("/put/sell-chronos")
    public ResponseEntity<UserResponseDTO> sellChronos(@RequestHeader("Authorization") String tokenHeader, @RequestHeader("Chronos") Integer chronos) {
        return ResponseEntity.ok(UserResponseDTO.fromEntity(userService.sellChronos(tokenHeader, chronos)));
    }

    @GetMapping("/get")
    public ResponseEntity<UserResponseDTO> getLoggedUser(@RequestHeader("Authorization") String tokenHeader) {
        return ResponseEntity.ok(UserResponseDTO.fromEntity(userService.getLoggedUser(tokenHeader)));
    }

    @GetMapping("/wallet/summary")
    public ResponseEntity<Map<String, Integer>> getWalletSummary(@RequestHeader("Authorization") String tokenHeader) {
        return ResponseEntity.ok(userService.getWalletSummary(tokenHeader));
    }

    @PutMapping("/put")
    public ResponseEntity<UserResponseDTO> put(@RequestHeader("Authorization") String tokenHeader, @RequestBody @Valid UserEditDTO userEditDTO) {
        logger.info("Editando usuário {}", userEditDTO.getId());
        UserEntity saved = userService.put(userEditDTO, tokenHeader);
        logger.info("Usuário editado com sucesso: {}", saved.getId());
        return ResponseEntity.ok(UserResponseDTO.fromEntity(saved));
    }

    @PutMapping("/fcm-token")
    public ResponseEntity<Void> saveFcmToken(@RequestHeader("Authorization") String tokenHeader, @RequestBody @Valid FcmTokenDTO dto) {
        userService.saveFcmToken(tokenHeader, dto.getToken());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String tokenHeader) {
        userService.delete(tokenHeader);
        return ResponseEntity.ok().build();
    }
}
