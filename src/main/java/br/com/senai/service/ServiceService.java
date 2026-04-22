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
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
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
            String jwtToken = extractBearerToken(tokenHeader);
            String imageUrl = storageService.uploadBase64Image(serviceDTO.getServiceImage(), "services", jwtToken);
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
            validateCategories(serviceEditDTO.getCategories());
            service.setCategoryEntities(buildCategories(serviceEditDTO.getCategories()));
        } else if (serviceEditDTO.getCategoryEntities() != null) {
            service.setCategoryEntities(copyCategories(serviceEditDTO.getCategoryEntities()));
        }

        if (serviceEditDTO.getServiceImage() != null && !serviceEditDTO.getServiceImage().isBlank()) {
            String jwtToken = extractBearerToken(tokenHeader);
            String imageUrl = storageService.uploadBase64Image(serviceEditDTO.getServiceImage(), "services", jwtToken);
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
    public List<ServiceEntity> getAll(String tokenHeader) {
        userService.getLoggedUser(tokenHeader);
        return serviceRepository.findAll();
    }

    @Transactional
    public List<ServiceEntity> getAllByStatus(ServiceStatus status, String tokenHeader) {
        userService.getLoggedUser(tokenHeader);
        return serviceRepository.findAllByStatus(status);
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
        return verificationCode.replace("\"", "").trim();
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

    private String extractBearerToken(String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            return tokenHeader.substring(7);
        }
        return tokenHeader;
    }
}
