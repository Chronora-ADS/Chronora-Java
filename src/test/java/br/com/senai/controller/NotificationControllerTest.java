package br.com.senai.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.senai.model.entity.NotificationEntity;
import br.com.senai.service.notification.NotificationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private static final String TOKEN_HEADER = "Bearer token-valido";

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void deveListarNotificacoesViaController() {
        NotificationEntity notification = new NotificationEntity();
        notification.setId(100L);
        notification.setMessage("Pedido cancelado");
        when(notificationService.getAll(TOKEN_HEADER)).thenReturn(List.of(notification));

        var response = notificationController.getAll(TOKEN_HEADER);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(notification), response.getBody());
        verify(notificationService).getAll(TOKEN_HEADER);
    }
}
