package br.com.senai.controller;

import br.com.senai.model.entity.NotificationEntity;
import br.com.senai.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @GetMapping("/get/all")
    public ResponseEntity<List<NotificationEntity>> getAll(@RequestHeader("Authorization") String tokenHeader) {
        logger.info("Listando todos as notificações");
        List<NotificationEntity> notifications = notificationService.getAll(tokenHeader);
        logger.info("Total de notificações encontrados: {}", notifications.size());
        return ResponseEntity.ok(notifications);
    }
}
