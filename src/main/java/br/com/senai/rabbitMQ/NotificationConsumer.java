package br.com.senai.rabbitMQ;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveNotification(String message) {
        // message será algo como "Serviço 123 com prazo expirado para usuário abc"
        if (message != null) {
            // Extrai o nome do usuário para enviar a notificação apenas para ele
            String userId = extractUserIdFromMessage(message); // 'abc'
            messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", message);
        }
    }

    // Método helper para processar a mensagem e extrair o userId
    private String extractUserIdFromMessage(String message) {
        try {
            return message.split("usuário ")[1].split("\\.")[0].trim();
        } catch (Exception e) {
            return "fallback-user";
        }
    }
}
