package br.com.senai.service;

import br.com.senai.model.DTO.NotificationEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventConsumer.class);

    @RabbitListener(queues = "${rabbitmq.notification.queue:chronora.notification.queue}")
    public void consume(NotificationEventDTO event) {
        logger.info("Processando notificação assíncrona: userId={}, serviceId={}, message={}",
                event.getUserId(), event.getServiceId(), event.getMessage());

        // ponto de extensão para integração de Push/E-mail
        logger.info("Notificação Push/E-mail simulada para {}", event.getUserEmail());
    }
}
