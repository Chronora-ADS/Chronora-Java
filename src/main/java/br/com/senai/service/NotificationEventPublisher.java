package br.com.senai.service;

import br.com.senai.model.DTO.NotificationEventDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String notificationExchange;
    private final String notificationRoutingKey;

    public NotificationEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${rabbitmq.notification.exchange:chronora.notification.exchange}") String notificationExchange,
            @Value("${rabbitmq.notification.routing-key:chronora.notification.created}") String notificationRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.notificationExchange = notificationExchange;
        this.notificationRoutingKey = notificationRoutingKey;
    }

    public void publish(NotificationEventDTO payload) {
        rabbitTemplate.convertAndSend(notificationExchange, notificationRoutingKey, payload);
    }
}
