package br.com.senai.controller;

import br.com.senai.model.entity.NotificationEntity;
import br.com.senai.service.NotificationService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<NotificationEntity>> getAll(@RequestHeader("Authorization") String tokenHeader) {
        logger.info("Listando todas as notificacoes");
        List<NotificationEntity> notifications = notificationService.getAll(tokenHeader);
        logger.info("Total de notificacoes encontradas: {}", notifications.size());
        return ResponseEntity.ok(notifications);
    }
}
