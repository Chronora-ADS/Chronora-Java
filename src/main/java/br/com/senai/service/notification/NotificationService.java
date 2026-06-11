package br.com.senai.service.notification;

import br.com.senai.model.DTO.notification.NotificationEventDTO;
import br.com.senai.model.entity.NotificationEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.NotificationRepository;
import br.com.senai.service.user.UserService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final NotificationEventPublisher notificationEventPublisher;

    public NotificationEntity create(String message, UserEntity user) {
        return create(message, user, null);
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
        event.setServiceId(service != null ? service.getId() : null);
        event.setCreatedAt(OffsetDateTime.now());
        try {
            notificationEventPublisher.publish(event);
        } catch (AmqpException exception) {
            LOGGER.warn(
                    "Não foi possível publicar o evento de notificação no RabbitMQ. A notificação foi salva. Motivo: {}",
                    exception.getMessage()
            );
        }
        return saved;
    }

    @Transactional
    public List<NotificationEntity> getAll(String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        return notificationRepository.findAllByUser(user);
    }

    public boolean exists(String message, UserEntity user, ServiceEntity service) {
        return notificationRepository.existsByUserAndServiceAndMessage(user, service, message);
    }

    @Transactional
    public void deleteByService(ServiceEntity service) {
        notificationRepository.deleteAllByService(service);
    }
}
