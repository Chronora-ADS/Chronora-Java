package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.ServiceNotFoundException;
import br.com.senai.exception.Validation.ReviewValidationException;
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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public ReviewService(
            ReviewRepository reviewRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository,
            UserService userService,
            NotificationService notificationService
    ) {
        this.reviewRepository = reviewRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ReviewEntity submitReview(Long serviceId, ReviewDTO reviewDTO, String tokenHeader) {
        UserEntity reviewer = userService.getLoggedUser(tokenHeader);

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException("Servico nao encontrado."));

        if (service.getStatus() != ServiceStatus.CONCLUIDO) {
            throw new ReviewValidationException("Apenas pedidos concluidos podem ser avaliados.");
        }

        boolean isCreator = Objects.equals(service.getUserCreator().getId(), reviewer.getId());
        boolean isAccepted = service.getUserAccepted() != null
                && Objects.equals(service.getUserAccepted().getId(), reviewer.getId());

        if (!isCreator && !isAccepted) {
            throw new AuthException("Você não tem permissão para avaliar este pedido.");
        }

        if (reviewRepository.existsByServiceAndReviewer(service, reviewer)) {
            throw new ReviewValidationException("Você já avaliou este pedido.");
        }

        UserEntity reviewee = isCreator ? service.getUserAccepted() : service.getUserCreator();
        if (reviewee == null) {
            throw new ReviewValidationException("Não é possível avaliar este pedido.");
        }

        if (isCreator) {
            service.setRatedByCreator(true);
        } else {
            service.setRatedByProvider(true);
        }
        serviceRepository.save(service);

        ReviewEntity review = new ReviewEntity();
        review.setService(service);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(reviewDTO.getRating());
        review.setCreatedAt(LocalDateTime.now());
        ReviewEntity saved = reviewRepository.save(review);

        Double avgRating = reviewRepository.calculateAverageRatingForUser(reviewee.getId());
        if (avgRating != null) {
            reviewee.setRating(avgRating);
            userRepository.save(reviewee);
        }

        notificationService.create("Você foi avaliado", reviewee, service);

        return saved;
    }
}
