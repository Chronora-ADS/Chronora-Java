package br.com.senai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.senai.model.DTO.NotificationEventDTO;
import br.com.senai.model.entity.NotificationEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.NotificationRepository;
import java.util.List;
import org.springframework.amqp.AmqpException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final String TOKEN_HEADER = "Bearer token-valido";

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void deveCriarNotificacaoEPublicarEvento() {
        UserEntity user = criarUsuario();
        ServiceEntity service = criarServico();
        when(notificationRepository.save(org.mockito.ArgumentMatchers.any(NotificationEntity.class)))
                .thenAnswer(invocation -> {
                    NotificationEntity notification = invocation.getArgument(0);
                    notification.setId(100L);
                    return notification;
                });

        NotificationEntity criada = notificationService.create("Pedido aceito", user, service);

        assertEquals(100L, criada.getId());
        assertEquals("Pedido aceito", criada.getMessage());
        assertSame(user, criada.getUser());
        assertSame(service, criada.getService());
        assertNotNull(criada.getNotificationTime());

        ArgumentCaptor<NotificationEventDTO> eventCaptor = ArgumentCaptor.forClass(NotificationEventDTO.class);
        verify(notificationEventPublisher).publish(eventCaptor.capture());
        NotificationEventDTO event = eventCaptor.getValue();
        assertEquals("Pedido aceito", event.getMessage());
        assertEquals(1L, event.getUserId());
        assertEquals("ana@chronora.com", event.getUserEmail());
        assertEquals(10L, event.getServiceId());
        assertNotNull(event.getCreatedAt());
    }

    @Test
    void deveManterNotificacaoSalvaQuandoRabbitMqFalhar() {
        UserEntity user = criarUsuario();
        ServiceEntity service = criarServico();
        when(notificationRepository.save(org.mockito.ArgumentMatchers.any(NotificationEntity.class)))
                .thenAnswer(invocation -> {
                    NotificationEntity notification = invocation.getArgument(0);
                    notification.setId(100L);
                    return notification;
                });
        doThrow(new AmqpException("RabbitMQ indisponivel"))
                .when(notificationEventPublisher)
                .publish(org.mockito.ArgumentMatchers.any(NotificationEventDTO.class));

        NotificationEntity criada = notificationService.create("Pedido criado", user, service);

        assertEquals(100L, criada.getId());
        assertEquals("Pedido criado", criada.getMessage());
        assertSame(user, criada.getUser());
        assertSame(service, criada.getService());
        assertNotNull(criada.getNotificationTime());
        verify(notificationEventPublisher)
                .publish(org.mockito.ArgumentMatchers.any(NotificationEventDTO.class));
    }

    @Test
    void deveCriarNotificacaoComDetalhesHistoricos() {
        UserEntity user = criarUsuario();
        ServiceEntity service = criarServico();
        when(notificationRepository.save(org.mockito.ArgumentMatchers.any(NotificationEntity.class)))
                .thenAnswer(invocation -> {
                    NotificationEntity notification = invocation.getArgument(0);
                    notification.setId(100L);
                    return notification;
                });

        NotificationEntity criada = notificationService.createWithDetails(
                "Justificativa de cancelamento do servico",
                user,
                service,
                "SERVICE_CANCELLATION_JUSTIFICATION",
                "Fornecedor nao respondeu.",
                "Ana",
                "Requisitante"
        );

        assertEquals(100L, criada.getId());
        assertEquals("Justificativa de cancelamento do servico", criada.getMessage());
        assertEquals("SERVICE_CANCELLATION_JUSTIFICATION", criada.getNotificationType());
        assertEquals("Fornecedor nao respondeu.", criada.getDetail());
        assertEquals("Ana", criada.getActorName());
        assertEquals("Requisitante", criada.getActorRole());
        assertSame(user, criada.getUser());
        assertSame(service, criada.getService());
        assertNotNull(criada.getNotificationTime());
        verify(notificationEventPublisher)
                .publish(org.mockito.ArgumentMatchers.any(NotificationEventDTO.class));
    }

    @Test
    void deveListarNotificacoesDoUsuarioLogado() {
        UserEntity user = criarUsuario();
        NotificationEntity notification = new NotificationEntity();
        notification.setId(100L);
        notification.setMessage("Pedido finalizado");
        notification.setUser(user);
        notification.setService(criarServico());

        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(user);
        when(notificationRepository.findAllByUser(user)).thenReturn(List.of(notification));

        List<NotificationEntity> notificacoes = notificationService.getAll(TOKEN_HEADER);

        assertEquals(List.of(notification), notificacoes);
        verify(userService).getLoggedUser(TOKEN_HEADER);
        verify(notificationRepository).findAllByUser(user);
    }

    @Test
    void deveExcluirNotificacoesPorServico() {
        ServiceEntity service = criarServico();

        notificationService.deleteByService(service);

        verify(notificationRepository).deleteAllByService(service);
    }

    @Test
    void deveVerificarSeNotificacaoJaExisteParaUsuarioServicoEMensagem() {
        UserEntity user = criarUsuario();
        ServiceEntity service = criarServico();
        when(notificationRepository.existsByUserAndServiceAndMessage(user, service, "Prazo do pedido chegou"))
                .thenReturn(true);

        boolean exists = notificationService.exists("Prazo do pedido chegou", user, service);

        assertEquals(true, exists);
        verify(notificationRepository).existsByUserAndServiceAndMessage(user, service, "Prazo do pedido chegou");
    }

    private UserEntity criarUsuario() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("Ana");
        user.setEmail("ana@chronora.com");
        return user;
    }

    private ServiceEntity criarServico() {
        ServiceEntity service = new ServiceEntity();
        service.setId(10L);
        service.setTitle("Aula de Java");
        return service;
    }
}
