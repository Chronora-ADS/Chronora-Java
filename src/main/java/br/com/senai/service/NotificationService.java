package br.com.senai.service;

import br.com.senai.model.DTO.NotificationEventDTO;
import br.com.senai.model.entity.NotificationEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final NotificationEventPublisher notificationEventPublisher;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserService userService,
            NotificationEventPublisher notificationEventPublisher
    ) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    public NotificationEntity create(String message, UserEntity user, ServiceEntity service) {
        NotificationEntity notification = new NotificationEntity();
        notification.setMessage(message);
        notification.setUser(user);
        notification.setService(service);
        notification.setNotificationTime(LocalDateTime.now());

        NotificationEntity saved = notificationRepository.save(notification);

        NotificationEventDTO event = new NotificationEventDTO();
        event.setMessage(saved.getMessage());
        event.setUserId(user.getId());
        event.setUserEmail(user.getEmail());
        event.setServiceId(service.getId());
        event.setCreatedAt(OffsetDateTime.now());
        notificationEventPublisher.publish(event);

        return saved;
    }

    @Transactional
    public List<NotificationEntity> getAll(String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        return notificationRepository.findAllByUser(user);
    }

    @Transactional
    public void deleteByService(ServiceEntity service) {
        notificationRepository.deleteAllByService(service);
    }
}
