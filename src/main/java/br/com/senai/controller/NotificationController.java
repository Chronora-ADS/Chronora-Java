package br.com.senai.controller;

import br.com.senai.model.entity.NotificationEntity;
import br.com.senai.service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<Page<NotificationEntity>> getAll(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        logger.info("Listando notificações (page={}, size={})", page, size);
        Page<NotificationEntity> result = notificationService.getAll(tokenHeader, page, size);
        logger.info("Total de notificações: {}", result.getTotalElements());
        return ResponseEntity.ok(result);
    }
}
