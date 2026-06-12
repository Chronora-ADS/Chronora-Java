package br.com.senai.model.DTO.notification;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class NotificationEventDTO {
    private String message;
    private Long userId;
    private String userEmail;
    private Long serviceId;
    private OffsetDateTime createdAt;
}
