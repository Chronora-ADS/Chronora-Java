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
import br.com.senai.model.enums.ServiceStatus;
import br.com.senai.repository.ServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private static final long VERIFICATION_CODE_EXPIRATION_MINUTES = 2;

    private final ServiceRepository serviceRepository;
    private final UserService userService;
    private final SupabaseStorageService storageService;
    private final NotificationService notificationService;

    public ServiceEntity create(ServiceDTO serviceDTO, String tokenHeader) {
        UserEntity userEntity = userService.getLoggedUser(tokenHeader);

        if (serviceDTO.getTimeChronos() > 100) {
            throw new QuantityChronosInvalidException("Limite de chronos de 100 por servico excedido.");
        }

        if (serviceDTO.getTimeChronos() > userEntity.getTimeChronos()) {
            throw new QuantityChronosInvalidException("Quantidade de Chronos do servico superior a quantidade em carteira.");
        }

        ServiceEntity service = new ServiceEntity();
        service.setTitle(serviceDTO.getTitle());
        service.setDescription(serviceDTO.getDescription());
        service.setTimeChronos(serviceDTO.getTimeChronos());
        userService.sellChronos(tokenHeader, serviceDTO.getTimeChronos());
        service.setDeadline(serviceDTO.getDeadline());
        service.setModality(serviceDTO.getModality());
        service.setPostedAt(LocalDateTime.now());
        service.setStatus(ServiceStatus.CRIADO);
        List<CategoryEntity> categories = new ArrayList<>();
        for (String category : serviceDTO.getCategories()) {
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setName(category);
            categories.add(categoryEntity);
        }
        service.setCategoryEntities(categories);
        service.setUserCreator(userEntity);

        if (serviceDTO.getServiceImage() != null && !serviceDTO.getServiceImage().isEmpty()) {
            String jwtToken = tokenHeader.substring(7);
            String imageUrl = storageService.uploadBase64Image(serviceDTO.getServiceImage(), "services", jwtToken);
            service.setServiceImageUrl(imageUrl);
        }

        service = serviceRepository.save(service);

        String notificationMessage = "Pedido criado";
        notificationService.create(notificationMessage, userEntity, service);

        return service;
    }

    public ServiceEntity put(ServiceEditDTO serviceEditDTO, String tokenHeader) {
        UserEntity userEntity = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getById(serviceEditDTO.getId());

        if (!Objects.equals(service.getUserCreator().getId(), userEntity.getId())) {
            throw new AuthException("Credenciais invalidas.");
        }

        if (serviceEditDTO.getTimeChronos() > 100) {
            throw new QuantityChronosInvalidException("Limite de chronos de 100 por servico excedido.");
        }

        if (serviceEditDTO.getTitle() != null) {
            service.setTitle(serviceEditDTO.getTitle());
        }
        if (serviceEditDTO.getDescription() != null) {
            service.setDescription(serviceEditDTO.getDescription());
        }
        if (serviceEditDTO.getTimeChronos() != null) {
            if ((serviceEditDTO.getTimeChronos() - service.getTimeChronos()) > userEntity.getTimeChronos()) {
                throw new QuantityChronosInvalidException("Quantidade de Chronos do servico superior a quantidade em carteira.");
            }
            userService.sellChronos(tokenHeader, serviceEditDTO.getTimeChronos() - service.getTimeChronos());
            service.setTimeChronos(serviceEditDTO.getTimeChronos());
        }
        if (serviceEditDTO.getDeadline() != null) {
            service.setDeadline(serviceEditDTO.getDeadline());
        }
        if (serviceEditDTO.getModality() != null) {
            service.setModality(serviceEditDTO.getModality());
        }
        if (serviceEditDTO.getCategoryEntities() != null) {
            service.setCategoryEntities(serviceEditDTO.getCategoryEntities());
        }
        if (serviceEditDTO.getServiceImage() != null) {
            String jwtToken = tokenHeader.substring(7);
            String imageUrl = storageService.uploadBase64Image(serviceEditDTO.getServiceImage(), "services", jwtToken);
            service.setServiceImageUrl(imageUrl);
        }

        service = serviceRepository.save(service);

        String notificationMessage = "Pedido editado";
        notificationService.create(notificationMessage, userEntity, service);

        return service;
    }

    private ServiceEntity changeStatus(Long id, ServiceStatus status) {
        ServiceEntity service = getById(id);
        service.setStatus(status);
        return serviceRepository.save(service);
    }

    private String generateVerificationCode() {
        Integer verificationCode = (int) (Math.random() * 10000);
        return String.format("%04d", verificationCode);
    }

    private void clearVerificationCode(ServiceEntity service) {
        service.setVerificationCode(null);
        service.setVerificationCodeExpiresAt(null);
    }

    public ServiceEntity acceptService(Long id, String tokenHeader) {
        UserEntity userAccepted = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getById(id);
        service.setStatus(ServiceStatus.ACEITO);
        service.setUserAccepted(userAccepted);
        service.setVerificationCode(generateVerificationCode());
        service.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES));
        service = serviceRepository.save(service);

        notificationService.create("Pedido aceito", userAccepted, service);
        notificationService.create("Pedido aceito por " + userAccepted.getName(), service.getUserCreator(), service);

        return service;
    }

    public ServiceEntity startService(Long id, String tokenHeader, String verificationCode) {
        UserEntity userAccepted = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getById(id);

        if (service.getVerificationCode() == null || service.getVerificationCodeExpiresAt() == null) {
            throw new IncorrectValidationCodeException("Codigo de verificacao indisponivel");
        }

        if (LocalDateTime.now().isAfter(service.getVerificationCodeExpiresAt())) {
            clearVerificationCode(service);
            serviceRepository.save(service);
            throw new ExpiredValidationCodeException("Codigo de verificacao expirado");
        }

        if (!Objects.equals(verificationCode, service.getVerificationCode())) {
            throw new IncorrectValidationCodeException("Codigo de verificacao incorreto");
        }

        service.setStatus(ServiceStatus.EM_ANDAMENTO);
        clearVerificationCode(service);
        service = serviceRepository.save(service);
        notificationService.create("Pedido iniciado", userAccepted, service);
        notificationService.create("Pedido iniciado", service.getUserCreator(), service);
        return service;
    }

    public ServiceEntity finishService(Long id, String tokenHeader) {
        userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getById(id);
        service.setStatus(ServiceStatus.CONCLUIDO);
        service = serviceRepository.save(service);

        notificationService.create("Pedido finalizado", service.getUserCreator(), service);
        notificationService.create("Pedido finalizado", service.getUserAccepted(), service);
        return service;
    }

    public ServiceEntity cancelService(Long id, String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getById(id);

        if (user == service.getUserCreator()) {
            service.setStatus(ServiceStatus.CANCELADO);
            clearVerificationCode(service);
            service = serviceRepository.save(service);
            notificationService.create("Pedido cancelado", user, service);
            if (service.getUserAccepted() != null) {
                notificationService.create("Pedido cancelado por " + user, service.getUserAccepted(), service);
            }
        } else {
            service.setUserAccepted(null);
            service.setStatus(ServiceStatus.CRIADO);
            clearVerificationCode(service);
            service = serviceRepository.save(service);
            notificationService.create("Pedido cancelado", user, service);
            notificationService.create("Pedido cancelado por " + user, service.getUserCreator(), service);
        }
        return service;
    }

    public ServiceEntity getById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Servico com ID " + id + " nao encontrado."));
    }

    @Transactional
    public List<ServiceEntity> getAll(String tokenHeader) {
        try {
            userService.getLoggedUser(tokenHeader);
            return serviceRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthException("Erro interno: " + e.getMessage());
        }
    }

    @Transactional
    public List<ServiceEntity> getAllByStatus(ServiceStatus status, String tokenHeader) {
        try {
            userService.getLoggedUser(tokenHeader);
            return serviceRepository.findAllByStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthException("Erro interno: " + e.getMessage());
        }
    }
}
