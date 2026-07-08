package br.com.senai.service.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class FcmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FcmService.class);

    @PostConstruct
    public void initialize() {
        String serviceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
        if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
            LOGGER.warn("FIREBASE_SERVICE_ACCOUNT_JSON não configurado — push notifications desativadas.");
            return;
        }
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream stream = new ByteArrayInputStream(
                        serviceAccountJson.getBytes(StandardCharsets.UTF_8));
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(stream))
                        .build();
                FirebaseApp.initializeApp(options);
                LOGGER.info("Firebase inicializado com sucesso.");
            }
        } catch (Exception e) {
            LOGGER.error("Falha ao inicializar Firebase: {}", e.getMessage());
        }
    }

    public void sendPushNotification(String fcmToken, String title, String body) {
        if (FirebaseApp.getApps().isEmpty()) return;
        if (fcmToken == null || fcmToken.isBlank()) return;

        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setToken(fcmToken)
                    .build();
            FirebaseMessaging.getInstance().send(message);
            LOGGER.info("Push enviado com sucesso.");
        } catch (Exception e) {
            LOGGER.warn("Falha ao enviar push notification: {}", e.getMessage());
        }
    }
}
