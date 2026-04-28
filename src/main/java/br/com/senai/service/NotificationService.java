package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.model.entity.NotificationEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationEntity create (String message, UserEntity user, ServiceEntity service) {
        NotificationEntity notification = new NotificationEntity();
        notification.setMessage(message);
        notification.setUser(user);
        notification.setService(service);
        notification.setNotificationTime(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Transactional
    public List<NotificationEntity> getAll(String tokenHeader) {
        try {
            UserEntity user = userService.getLoggedUser(tokenHeader);
            return notificationRepository.findAllByUser(user);
        } catch (Exception e) {
            e.printStackTrace(); // Isso vai mostrar o erro REAL no console
            throw new AuthException("Erro interno: " + e.getMessage());
        }
    }
}
