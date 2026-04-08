package br.com.senai.service;

import br.com.senai.model.entity.NotificationEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserService userService
    ) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    public NotificationEntity create(String message, UserEntity user, ServiceEntity service) {
        NotificationEntity notification = new NotificationEntity();
        notification.setMessage(message);
        notification.setUser(user);
        notification.setService(service);
        notification.setNotificationTime(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Transactional
    public List<NotificationEntity> getAll(String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        return notificationRepository.findAllByUser(user);
    }
}
