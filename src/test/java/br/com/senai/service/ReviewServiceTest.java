package br.com.senai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.senai.model.DTO.ReviewDTO;
import br.com.senai.model.entity.ReviewEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.model.enums.ServiceStatus;
import br.com.senai.repository.ReviewRepository;
import br.com.senai.repository.ServiceRepository;
import br.com.senai.repository.UserRepository;
import br.com.senai.service.notification.NotificationService;
import br.com.senai.service.user.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private static final String TOKEN_HEADER = "Bearer token-valido";

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void deveRecalcularMediaDoUsuarioAvaliadoAoEnviarAvaliacao() {
        UserEntity solicitante = criarUsuario(1L, "Ana");
        UserEntity fornecedor = criarUsuario(2L, "Bruno");
        ServiceEntity service = criarServicoConcluido(10L, solicitante, fornecedor);
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setRating(5.0);

        when(userService.getLoggedUser(TOKEN_HEADER)).thenReturn(solicitante);
        when(serviceRepository.findById(10L)).thenReturn(Optional.of(service));
        when(reviewRepository.existsByServiceAndReviewer(service, solicitante)).thenReturn(false);
        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewRepository.calculateAverageRatingForUser(2L)).thenReturn(4.5);

        ReviewEntity saved = reviewService.submitReview(10L, reviewDTO, TOKEN_HEADER);

        assertSame(service, saved.getService());
        assertSame(solicitante, saved.getReviewer());
        assertSame(fornecedor, saved.getReviewee());
        assertEquals(5.0, saved.getRating());
        assertEquals(4.5, fornecedor.getRating());
        assertEquals(true, service.isRatedByCreator());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        assertSame(fornecedor, userCaptor.getValue());
        assertEquals(4.5, userCaptor.getValue().getRating());
        verify(notificationService).create("Você foi avaliado", fornecedor, service);
    }

    private ServiceEntity criarServicoConcluido(Long id, UserEntity solicitante, UserEntity fornecedor) {
        ServiceEntity service = new ServiceEntity();
        service.setId(id);
        service.setStatus(ServiceStatus.CONCLUIDO);
        service.setUserCreator(solicitante);
        service.setUserAccepted(fornecedor);
        return service;
    }

    private UserEntity criarUsuario(Long id, String name) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setName(name);
        user.setEmail(name.toLowerCase() + "@chronora.com");
        user.setPhoneNumber(11999999999L + id);
        user.setPassword("hash");
        user.setTimeChronos(100);
        return user;
    }
}
