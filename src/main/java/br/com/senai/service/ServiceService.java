package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.ServiceNotFoundException;
import br.com.senai.exception.Validation.ExpiredValidationCodeException;
import br.com.senai.exception.Validation.IncorrectValidationCodeException;
import br.com.senai.exception.Validation.QuantityChronosInvalidException;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ServiceService {

    private static final long VERIFICATION_CODE_EXPIRATION_MINUTES = 2;
    private static final int MAX_CATEGORY_COUNT = 10;
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
        service.setVerificationCode(generateVerificationCode());
        service.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES));
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
            reopenAcceptedService(service);
            notificationService.create("Tempo para iniciar o pedido expirou", service.getUserCreator(), service);
            notificationService.create("Tempo para iniciar o pedido expirou", acceptedUser, service);
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
        notificationService.create("Tempo para iniciar o pedido expirou", service.getUserCreator(), service);
        notificationService.create("Tempo para iniciar o pedido expirou", acceptedUser, service);
        return service;
    }

    @Transactional
    public ServiceEntity finishService(Long id, String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getByIdForUpdate(id);

        if (!canManageAcceptedService(service, user)) {
            throw new AuthException("Credenciais invalidas.");
        }

        boolean isProvider = service.getUserAccepted() != null
                && Objects.equals(service.getUserAccepted().getId(), user.getId());

        if (isProvider) {
            if (service.getStatus() != ServiceStatus.EM_ANDAMENTO) {
                throw new AuthException("Este pedido nao esta em andamento.");
            }
            service.setStatus(ServiceStatus.AGUARDANDO_CONFIRMACAO);
            service = serviceRepository.save(service);
            notificationService.create(
                    "O prestador concluiu o servico. Confirme para finalizar o pedido.",
                    service.getUserCreator(),
                    service
            );
            notificationService.create(
                    "Pedido concluido com sucesso",
                    service.getUserAccepted(),
                    service
            );
            return service;
        }

        // Solicitante confirma a conclusao
        if (service.getStatus() != ServiceStatus.AGUARDANDO_CONFIRMACAO) {
            throw new AuthException("O prestador ainda nao concluiu o servico.");
        }

        if (service.getUserAccepted() != null) {
            userService.creditChronosToUser(service.getUserAccepted(), service.getTimeChronos());
        }

        service.setStatus(ServiceStatus.CONCLUIDO);
        clearVerificationCode(service);
        service = serviceRepository.save(service);

        notificationService.create("Pedido finalizado", service.getUserCreator(), service);
        if (service.getUserAccepted() != null) {
            notificationService.create("Solicitante finalizou o pedido", service.getUserAccepted(), service);
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
