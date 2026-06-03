package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.ServiceNotFoundException;
import br.com.senai.exception.Validation.ExpiredValidationCodeException;
import br.com.senai.exception.Validation.IncorrectValidationCodeException;
import br.com.senai.exception.Validation.QuantityChronosInvalidException;
import br.com.senai.model.DTO.ServiceCancellationDTO;
import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.DTO.ServiceEditDTO;
import br.com.senai.model.entity.CategoryEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.model.enums.ServiceModality;
import br.com.senai.model.enums.ServiceStatus;
import br.com.senai.repository.ServiceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ServiceService {

    private static final long VERIFICATION_CODE_EXPIRATION_MINUTES = 2;
    private static final int FIRST_VERIFICATION_CODE_CALL = 1;
    private static final int SECOND_VERIFICATION_CODE_CALL = 2;
    private static final int MAX_CATEGORY_COUNT = 10;
    private static final int MAX_CANCELLATION_JUSTIFICATION_LENGTH = 1000;
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    public static final String DEADLINE_ACTION_MESSAGE =
            "Prazo do pedido chegou. Renove o prazo ou cancele o pedido.";
    public static final String DEADLINE_AUTO_CANCEL_MESSAGE =
            "Pedido cancelado automaticamente por prazo expirado.";
    public static final String DEADLINE_RENEWED_MESSAGE = "Prazo do pedido renovado.";
    private static final ZoneId DEADLINE_ZONE = ZoneId.of("America/Sao_Paulo");

    private final ServiceRepository serviceRepository;
    private final UserService userService;
    private final SupabaseStorageService storageService;
    private final NotificationService notificationService;

    public ServiceService(
            ServiceRepository serviceRepository,
            UserService userService,
            SupabaseStorageService storageService,
            NotificationService notificationService
    ) {
        this.serviceRepository = serviceRepository;
        this.userService = userService;
        this.storageService = storageService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ServiceEntity create(ServiceDTO serviceDTO, String tokenHeader) {
        UserEntity userEntity = userService.getLoggedUser(tokenHeader);
        validateServiceChronos(serviceDTO.getTimeChronos());
        validateDescription(serviceDTO.getDescription());
        validateCategories(serviceDTO.getCategories());

        if (serviceDTO.getTimeChronos() > userEntity.getTimeChronos()) {
            throw new QuantityChronosInvalidException("Quantidade de chronos do servico superior a quantidade em carteira.");
        }

        ServiceEntity service = new ServiceEntity();
        service.setTitle(serviceDTO.getTitle());
        service.setDescription(serviceDTO.getDescription());
        service.setTimeChronos(serviceDTO.getTimeChronos());
        service.setDeadline(serviceDTO.getDeadline());
        service.setModality(ServiceModality.fromValue(serviceDTO.getModality()));
        service.setPostedAt(LocalDateTime.now());
        service.setStatus(ServiceStatus.CRIADO);
        service.setCategoryEntities(buildCategories(serviceDTO.getCategories()));
        service.setUserCreator(userEntity);

        if (serviceDTO.getServiceImage() != null && !serviceDTO.getServiceImage().isBlank()) {
            String imageUrl = storageService.uploadBase64Image(serviceDTO.getServiceImage(), "services", null);
            service.setServiceImageUrl(imageUrl);
        }

        userService.sellChronos(tokenHeader, serviceDTO.getTimeChronos());
        service = serviceRepository.save(service);
        notificationService.create("Pedido criado", userEntity, service);
        return service;
    }

    @Transactional
    public ServiceEntity put(ServiceEditDTO serviceEditDTO, String tokenHeader) {
        UserEntity userEntity = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getById(serviceEditDTO.getId());

        if (!Objects.equals(service.getUserCreator().getId(), userEntity.getId())) {
            throw new AuthException("Credenciais invalidas.");
        }

        if (serviceEditDTO.getTitle() != null && !serviceEditDTO.getTitle().isBlank()) {
            service.setTitle(serviceEditDTO.getTitle().trim());
        }

        if (serviceEditDTO.getDescription() != null) {
            validateDescription(serviceEditDTO.getDescription());
            service.setDescription(serviceEditDTO.getDescription());
        }

        if (serviceEditDTO.getTimeChronos() != null) {
            validateServiceChronos(serviceEditDTO.getTimeChronos());

            int chronosDifference = serviceEditDTO.getTimeChronos() - service.getTimeChronos();
            if (chronosDifference > 0) {
                if (chronosDifference > userEntity.getTimeChronos()) {
                    throw new QuantityChronosInvalidException("Quantidade de chronos do servico superior a quantidade em carteira.");
                }
                userService.sellChronos(tokenHeader, chronosDifference);
            } else if (chronosDifference < 0) {
                userService.buyChronos(tokenHeader, -chronosDifference);
            }

            service.setTimeChronos(serviceEditDTO.getTimeChronos());
        }

        if (serviceEditDTO.getDeadline() != null) {
            service.setDeadline(serviceEditDTO.getDeadline());
        }

        if (serviceEditDTO.getModality() != null) {
            service.setModality(ServiceModality.fromValue(serviceEditDTO.getModality()));
        }

        if (serviceEditDTO.getCategories() != null) {
            service.setCategoryEntities(buildCategories(serviceEditDTO.getCategories()));
        } else if (serviceEditDTO.getCategoryEntities() != null) {
            service.setCategoryEntities(copyCategories(serviceEditDTO.getCategoryEntities()));
        }

        if (serviceEditDTO.getServiceImage() != null && !serviceEditDTO.getServiceImage().isBlank()) {
            String imageUrl = storageService.uploadBase64Image(serviceEditDTO.getServiceImage(), "services", null);
            service.setServiceImageUrl(imageUrl);
        }

        service = serviceRepository.save(service);
        notificationService.create("Pedido editado", userEntity, service);
        return service;
    }

    @Transactional
    public ServiceEntity acceptService(Long id, String tokenHeader) {
        UserEntity userAccepted = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getByIdForUpdate(id);

        if (isAcceptedByAnotherUser(service, userAccepted)) {
            throw new AuthException("Pedido ja foi aceito por outro usuario.");
        }

        if (Objects.equals(service.getUserCreator().getId(), userAccepted.getId())) {
            throw new AuthException("Voce nao pode aceitar o proprio pedido.");
        }

        if (service.getStatus() == ServiceStatus.EM_ANDAMENTO
                || service.getStatus() == ServiceStatus.CONCLUIDO
                || service.getStatus() == ServiceStatus.CANCELADO) {
            throw new AuthException("Este pedido nao pode mais ser aceito.");
        }

        if (service.getUserAccepted() != null
                && Objects.equals(service.getUserAccepted().getId(), userAccepted.getId())
                && service.getStatus() == ServiceStatus.ACEITO) {
            return service;
        }

        service.setStatus(ServiceStatus.ACEITO);
        service.setUserAccepted(userAccepted);
        startVerificationCodeCall(service, FIRST_VERIFICATION_CODE_CALL);
        service = serviceRepository.save(service);

        notificationService.create("Pedido aceito", userAccepted, service);
        notificationService.create("Pedido aceito por " + userAccepted.getName(), service.getUserCreator(), service);
        return service;
    }

    @Transactional
    public ServiceEntity startService(Long id, String tokenHeader, String verificationCode) {
        UserEntity userAccepted = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getByIdForUpdate(id);

        if (service.getUserAccepted() == null
                || !Objects.equals(service.getUserAccepted().getId(), userAccepted.getId())) {
            throw new AuthException("Credenciais invalidas.");
        }

        if (service.getVerificationCode() == null || service.getVerificationCodeExpiresAt() == null) {
            throw new IncorrectValidationCodeException("Codigo de verificacao indisponivel");
        }

        if (LocalDateTime.now().isAfter(service.getVerificationCodeExpiresAt())) {
            UserEntity acceptedUser = service.getUserAccepted();
            if (isFinalVerificationCodeCall(service)) {
                reopenAcceptedService(service);
                notificationService.create("Segunda chamada expirada", service.getUserCreator(), service);
                notificationService.create("Segunda chamada expirada", acceptedUser, service);
            }
            throw new ExpiredValidationCodeException("Codigo de verificacao expirado");
        }

        String normalizedVerificationCode = normalizeVerificationCode(verificationCode);
        if (!Objects.equals(normalizedVerificationCode, service.getVerificationCode())) {
            throw new IncorrectValidationCodeException("Codigo de verificacao incorreto");
        }

        clearVerificationCode(service);
        service.setStatus(ServiceStatus.EM_ANDAMENTO);
        service = serviceRepository.save(service);

        notificationService.create("Pedido iniciado", userAccepted, service);
        notificationService.create("Pedido iniciado", service.getUserCreator(), service);
        return service;
    }

    @Transactional
    public ServiceEntity expireAcceptedService(Long id, String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getByIdForUpdate(id);

        if (service.getStatus() != ServiceStatus.ACEITO
                || service.getUserAccepted() == null
                || service.getVerificationCodeExpiresAt() == null) {
            return service;
        }

        if (!canManageAcceptedService(service, user)) {
            throw new AuthException("Credenciais invalidas.");
        }

        if (LocalDateTime.now().isBefore(service.getVerificationCodeExpiresAt())) {
            return service;
        }

        UserEntity acceptedUser = service.getUserAccepted();
        reopenAcceptedService(service);
        notificationService.create("Segunda chamada expirada", service.getUserCreator(), service);
        notificationService.create("Segunda chamada expirada", acceptedUser, service);
        return service;
    }

    @Transactional
    public ServiceEntity secondCall(Long id, String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getByIdForUpdate(id);

        if (!Objects.equals(service.getUserCreator().getId(), user.getId())) {
            throw new AuthException("Somente o solicitante pode iniciar a segunda chamada.");
        }

        if (service.getStatus() != ServiceStatus.ACEITO
                || service.getUserAccepted() == null
                || service.getVerificationCodeExpiresAt() == null) {
            throw new AuthException("Este pedido nao esta aguardando segunda chamada.");
        }

        if (LocalDateTime.now().isBefore(service.getVerificationCodeExpiresAt())) {
            throw new AuthException("A segunda chamada so pode ser iniciada apos o codigo expirar.");
        }

        if (isFinalVerificationCodeCall(service)) {
            throw new AuthException("A segunda chamada ja foi utilizada.");
        }

        startVerificationCodeCall(service, SECOND_VERIFICATION_CODE_CALL);
        service = serviceRepository.save(service);

        notificationService.create("Segunda chamada iniciada", service.getUserCreator(), service);
        notificationService.create("Segunda chamada iniciada", service.getUserAccepted(), service);
        return service;
    }

    @Transactional
    public ServiceEntity finishService(Long id, String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getByIdForUpdate(id);

        if (!canManageAcceptedService(service, user)) {
            throw new AuthException("Credenciais invalidas.");
        }

        service.setStatus(ServiceStatus.CONCLUIDO);
        clearVerificationCode(service);
        service = serviceRepository.save(service);

        notificationService.create("Pedido finalizado", service.getUserCreator(), service);
        if (service.getUserAccepted() != null) {
            notificationService.create("Pedido finalizado", service.getUserAccepted(), service);
        }
        return service;
    }

    @Transactional
    public ServiceEntity cancelService(Long id, String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getByIdForUpdate(id);

        if (Objects.equals(user.getId(), service.getUserCreator().getId())) {
            service.setStatus(ServiceStatus.CANCELADO);
            clearVerificationCode(service);
            service = serviceRepository.save(service);

            notificationService.create("Pedido cancelado", user, service);
            if (service.getUserAccepted() != null) {
                notificationService.create("Pedido cancelado por " + user.getName(), service.getUserAccepted(), service);
            }
            return service;
        }

        if (service.getUserAccepted() == null || !Objects.equals(user.getId(), service.getUserAccepted().getId())) {
            throw new AuthException("Credenciais invalidas.");
        }

        if (service.getStatus() == ServiceStatus.ACEITO) {
            reopenAcceptedService(service);
        } else {
            clearVerificationCode(service);
            service.setUserAccepted(null);
            service.setStatus(ServiceStatus.CANCELADO);
            service = serviceRepository.save(service);
        }

        notificationService.create("Pedido cancelado", user, service);
        notificationService.create("Pedido cancelado por " + user.getName(), service.getUserCreator(), service);
        return service;
    }

    @Transactional
    public ServiceEntity cancelAcceptedService(Long id, String tokenHeader, ServiceCancellationDTO cancellationDTO) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getByIdForUpdate(id);

        if (!canManageAcceptedService(service, user)) {
            throw new AuthException("Credenciais invalidas.");
        }

        if (service.getStatus() != ServiceStatus.ACEITO || service.getUserAccepted() == null) {
            throw new AuthException("Este servico nao pode ser cancelado para reabertura.");
        }

        UserEntity otherUser = Objects.equals(user.getId(), service.getUserCreator().getId())
                ? service.getUserAccepted()
                : service.getUserCreator();

        service.setServiceCancellationRequestedByUserId(user.getId());
        if (hasCancellationJustification(cancellationDTO)) {
            service.setServiceCancellationJustification(
                    normalizeCancellationJustification(cancellationDTO.getJustification())
            );
        } else {
            service.setServiceCancellationJustification(null);
        }
        reopenAcceptedService(service);

        notificationService.create("Servico cancelado", user, service);
        notificationService.create("Servico cancelado por " + user.getName(), otherUser, service);
        return service;
    }

    @Transactional
    public ServiceEntity registerServiceCancellationJustification(Long id, String tokenHeader, ServiceCancellationDTO cancellationDTO) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getByIdForUpdate(id);

        if (!Objects.equals(service.getServiceCancellationRequestedByUserId(), user.getId())) {
            throw new AuthException("Somente o usuario que cancelou o servico pode registrar a justificativa.");
        }

        service.setServiceCancellationJustification(
                normalizeCancellationJustification(cancellationDTO != null ? cancellationDTO.getJustification() : null)
        );
        service = serviceRepository.save(service);

        notificationService.create("Justificativa de cancelamento registrada", user, service);
        return service;
    }

    @Transactional
    public ServiceEntity renewDeadline(Long id, String tokenHeader, LocalDate newDeadline) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getByIdForUpdate(id);

        if (!Objects.equals(user.getId(), service.getUserCreator().getId())) {
            throw new AuthException("Credenciais invalidas.");
        }

        if (service.getStatus() != ServiceStatus.CRIADO) {
            throw new AuthException("Somente pedidos criados podem ter prazo renovado.");
        }

        if (newDeadline == null || !newDeadline.isAfter(LocalDate.now(DEADLINE_ZONE))) {
            throw new IllegalArgumentException("Novo prazo deve ser uma data futura.");
        }

        service.setDeadline(newDeadline);
        service = serviceRepository.save(service);
        notificationService.create(DEADLINE_RENEWED_MESSAGE, user, service);
        return service;
    }

    @Transactional
    public void processDeadlineRules() {
        processDeadlineRules(LocalDate.now(DEADLINE_ZONE));
    }

    @Transactional
    public void processDeadlineRules(LocalDate today) {
        if (today == null) {
            throw new IllegalArgumentException("Data de processamento e obrigatoria.");
        }

        notifyServicesDueToday(today);
        cancelExpiredServices(today);
    }

    @Transactional
    public void deleteService(Long id, String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getById(id);

        if (!Objects.equals(user.getId(), service.getUserCreator().getId())) {
            throw new AuthException("Credenciais invalidas.");
        }

        if (!canHardDelete(service)) {
            throw new AuthException("Somente pedidos criados ou aceitos podem ser excluidos.");
        }

        userService.buyChronos(tokenHeader, service.getTimeChronos());
        notificationService.deleteByService(service);
        serviceRepository.delete(service);
    }

    public ServiceEntity getById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Servico com ID " + id + " nao encontrado."));
    }

    public ServiceEntity getByIdForUpdate(Long id) {
        return serviceRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ServiceNotFoundException("Servico com ID " + id + " nao encontrado."));
    }

    @Transactional
    public Page<ServiceEntity> getAll(String tokenHeader, int page, int size) {
        userService.getLoggedUser(tokenHeader);
        return serviceRepository.findAll(PageRequest.of(page, size));
    }

    @Transactional
    public Page<ServiceEntity> getAllByStatus(ServiceStatus status, String tokenHeader, int page, int size) {
        userService.getLoggedUser(tokenHeader);
        return serviceRepository.findAllByStatus(status, PageRequest.of(page, size));
    }

    @Transactional
    public Page<ServiceEntity> searchByStatus(
            ServiceStatus status,
            String tokenHeader,
            int page,
            int size,
            String query,
            List<String> categories,
            String modality,
            LocalDate deadline,
            Integer minTimeChronos,
            Integer maxTimeChronos,
            String sort
    ) {
        userService.getLoggedUser(tokenHeader);

        Specification<ServiceEntity> specification = buildStatusSearchSpecification(
                status,
                query,
                categories,
                modality,
                deadline,
                minTimeChronos,
                maxTimeChronos
        );

        return serviceRepository.findAll(specification, PageRequest.of(page, size, buildServiceSort(sort)));
    }

    private Specification<ServiceEntity> buildStatusSearchSpecification(
            ServiceStatus status,
            String query,
            List<String> categories,
            String modality,
            LocalDate deadline,
            Integer minTimeChronos,
            Integer maxTimeChronos
    ) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("status"), status));

            String normalizedQuery = normalizeFilterText(query);
            if (normalizedQuery != null) {
                String searchPattern = "%" + normalizedQuery + "%";
                Join<ServiceEntity, CategoryEntity> categoryJoin = root.join("categoryEntities", JoinType.LEFT);
                Join<ServiceEntity, UserEntity> creatorJoin = root.join("userCreator", JoinType.LEFT);

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(creatorJoin.get("name")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("modality").as(String.class)), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(categoryJoin.get("name")), searchPattern)
                ));
            }

            List<String> normalizedCategories = normalizeFilterList(categories);
            if (!normalizedCategories.isEmpty()) {
                Join<ServiceEntity, CategoryEntity> categoryJoin = root.join("categoryEntities", JoinType.LEFT);
                List<Predicate> categoryPredicates = normalizedCategories.stream()
                        .map(category -> criteriaBuilder.like(
                                criteriaBuilder.lower(categoryJoin.get("name")),
                                "%" + category + "%"
                        ))
                        .toList();

                predicates.add(criteriaBuilder.or(categoryPredicates.toArray(Predicate[]::new)));
            }

            ServiceModality selectedModality = parseFilterModality(modality);
            if (selectedModality != null) {
                predicates.add(criteriaBuilder.equal(root.get("modality"), selectedModality));
            }

            if (deadline != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("deadline"), deadline));
            }

            if (minTimeChronos != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timeChronos"), minTimeChronos));
            }

            if (maxTimeChronos != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timeChronos"), maxTimeChronos));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Sort buildServiceSort(String sort) {
        if ("1".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "id");
        }

        if ("3".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "timeChronos").and(Sort.by(Sort.Direction.DESC, "id"));
        }

        if ("4".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "timeChronos").and(Sort.by(Sort.Direction.DESC, "id"));
        }

        return Sort.by(Sort.Direction.DESC, "id");
    }

    private String normalizeFilterText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private List<String> normalizeFilterList(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .map(this::normalizeFilterText)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private ServiceModality parseFilterModality(String modality) {
        if (modality == null || modality.isBlank()) {
            return null;
        }

        return ServiceModality.fromValue(modality);
    }

    private List<CategoryEntity> buildCategories(List<String> categories) {
        validateCategories(categories);

        List<CategoryEntity> categoryEntities = new ArrayList<>();
        for (String category : categories) {
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setName(category.trim());
            categoryEntities.add(categoryEntity);
        }
        return categoryEntities;
    }

    private List<CategoryEntity> copyCategories(List<CategoryEntity> categories) {
        List<String> categoryNames = categories.stream()
                .map(CategoryEntity::getName)
                .toList();
        return buildCategories(categoryNames);
    }

    private ServiceEntity reopenAcceptedService(ServiceEntity service) {
        clearVerificationCode(service);
        service.setUserAccepted(null);
        service.setStatus(ServiceStatus.CRIADO);
        return serviceRepository.save(service);
    }

    private void notifyServicesDueToday(LocalDate today) {
        List<ServiceEntity> servicesDueToday =
                serviceRepository.findAllByStatusAndDeadline(ServiceStatus.CRIADO, today);

        for (ServiceEntity service : servicesDueToday) {
            UserEntity owner = service.getUserCreator();
            boolean alreadyNotified = notificationService.exists(DEADLINE_ACTION_MESSAGE, owner, service);
            if (!alreadyNotified) {
                notificationService.create(DEADLINE_ACTION_MESSAGE, owner, service);
            }
        }
    }

    private void cancelExpiredServices(LocalDate today) {
        List<ServiceEntity> expiredServices =
                serviceRepository.findAllByStatusAndDeadlineBefore(ServiceStatus.CRIADO, today);

        for (ServiceEntity service : expiredServices) {
            clearVerificationCode(service);
            service.setStatus(ServiceStatus.CANCELADO);
            ServiceEntity savedService = serviceRepository.save(service);
            notificationService.create(DEADLINE_AUTO_CANCEL_MESSAGE, savedService.getUserCreator(), savedService);
        }
    }

    private void clearVerificationCode(ServiceEntity service) {
        service.setVerificationCode(null);
        service.setVerificationCodeExpiresAt(null);
        service.setVerificationCodeCallCount(0);
    }

    private void startVerificationCodeCall(ServiceEntity service, int callCount) {
        service.setVerificationCode(generateVerificationCode());
        service.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES));
        service.setVerificationCodeCallCount(callCount);
    }

    private boolean isFinalVerificationCodeCall(ServiceEntity service) {
        return getVerificationCodeCallCount(service) >= SECOND_VERIFICATION_CODE_CALL;
    }

    private int getVerificationCodeCallCount(ServiceEntity service) {
        Integer callCount = service.getVerificationCodeCallCount();
        if (callCount != null && callCount > 0) {
            return callCount;
        }
        return service.getVerificationCode() == null ? 0 : FIRST_VERIFICATION_CODE_CALL;
    }

    private String generateVerificationCode() {
        return String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private String normalizeVerificationCode(String verificationCode) {
        if (verificationCode == null) {
            return null;
        }

        String trimmedVerificationCode = verificationCode.trim();
        if (trimmedVerificationCode.isEmpty()) {
            return trimmedVerificationCode;
        }

        try {
            JsonNode codePayload = JSON_MAPPER.readTree(trimmedVerificationCode);
            if (codePayload.isTextual() || codePayload.isNumber()) {
                return codePayload.asText().trim();
            }

            if (codePayload.isObject()) {
                for (String key : List.of("code", "verificationCode", "authenticationCode")) {
                    JsonNode codeNode = codePayload.get(key);
                    if (codeNode != null && (codeNode.isTextual() || codeNode.isNumber())) {
                        return codeNode.asText().trim();
                    }
                }
            }
        } catch (JsonProcessingException ignored) {
            // Mantem compatibilidade com chamadas antigas que enviavam texto puro.
        }

        return trimmedVerificationCode.replace("\"", "").trim();
    }

    private boolean isAcceptedByAnotherUser(ServiceEntity service, UserEntity user) {
        return service.getUserAccepted() != null
                && !Objects.equals(service.getUserAccepted().getId(), user.getId());
    }

    private boolean canManageAcceptedService(ServiceEntity service, UserEntity user) {
        return Objects.equals(service.getUserCreator().getId(), user.getId())
                || (service.getUserAccepted() != null
                && Objects.equals(service.getUserAccepted().getId(), user.getId()));
    }

    private boolean canHardDelete(ServiceEntity service) {
        return service.getStatus() == ServiceStatus.CRIADO || service.getStatus() == ServiceStatus.ACEITO;
    }

    private String normalizeCancellationJustification(String justification) {
        if (justification == null || justification.isBlank()) {
            throw new IllegalArgumentException("Justificativa do cancelamento e obrigatoria");
        }

        String normalizedJustification = justification.trim();
        if (normalizedJustification.length() > MAX_CANCELLATION_JUSTIFICATION_LENGTH) {
            throw new IllegalArgumentException("Justificativa do cancelamento deve ter no maximo 1000 caracteres");
        }
        return normalizedJustification;
    }

    private boolean hasCancellationJustification(ServiceCancellationDTO cancellationDTO) {
        return cancellationDTO != null
                && cancellationDTO.getJustification() != null
                && !cancellationDTO.getJustification().isBlank();
    }

    private void validateServiceChronos(Integer timeChronos) {
        if (timeChronos == null || timeChronos <= 0) {
            throw new QuantityChronosInvalidException("A quantidade de chronos do servico deve ser maior que zero.");
        }
        if (timeChronos > 100) {
            throw new QuantityChronosInvalidException("Limite de chronos de 100 por servico excedido.");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Descricao do servico e obrigatoria");
        }
        if (description.length() > 2500) {
            throw new IllegalArgumentException("Descricao do servico deve ter no maximo 2500 caracteres");
        }
    }

    private void validateCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("Categoria do servico e obrigatoria");
        }
        if (categories.size() > MAX_CATEGORY_COUNT) {
            throw new IllegalArgumentException("O servico pode ter no maximo 10 categorias");
        }

        boolean hasBlankCategory = categories.stream()
                .anyMatch(category -> category == null || category.isBlank());
        if (hasBlankCategory) {
            throw new IllegalArgumentException("Categoria do servico e obrigatoria");
        }
    }

}
