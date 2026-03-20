package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.ServiceNotFoundException;
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

    private final ServiceRepository serviceRepository;
    private final UserService userService;
    private final SupabaseStorageService storageService;
    private final NotificationService notificationService;

    public ServiceEntity create(ServiceDTO serviceDTO, String tokenHeader) {
        UserEntity userEntity = userService.getLoggedUser(tokenHeader);

        if (serviceDTO.getTimeChronos() > 100) {
            throw new QuantityChronosInvalidException("Limite de chronos de 100 por serviço excedido.");
        }

        if (serviceDTO.getTimeChronos() > userEntity.getTimeChronos()) {
            throw new QuantityChronosInvalidException("Quantidade de Chronos do serviço superior à quantidade em carteira.");
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

        String notificationMessage = "Pedido criado";
        notificationService.create(notificationMessage, userEntity, service);

        return serviceRepository.save(service);
    }

    public ServiceEntity put(ServiceEditDTO serviceEditDTO, String tokenHeader) {
        UserEntity userEntity = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getById(serviceEditDTO.getId());

        if (!Objects.equals(service.getUserCreator().getId(), userEntity.getId())) {
            throw new AuthException("Credenciais inválidas.");
        }

        if (serviceEditDTO.getTimeChronos() > 100) {
            throw new QuantityChronosInvalidException("Limite de chronos de 100 por serviço excedido.");
        }

        if(serviceEditDTO.getTitle() != null) {
            service.setTitle(serviceEditDTO.getTitle());
        }
        if (serviceEditDTO.getDescription() != null) {
            service.setDescription(serviceEditDTO.getDescription());
        }
        if(serviceEditDTO.getTimeChronos() != null) {
            if ((serviceEditDTO.getTimeChronos() - service.getTimeChronos()) > userEntity.getTimeChronos()) {
                throw new QuantityChronosInvalidException("Quantidade de Chronos do serviço superior à quantidade em carteira.");
            }
            userService.sellChronos(tokenHeader, serviceEditDTO.getTimeChronos() - service.getTimeChronos());
            service.setTimeChronos(serviceEditDTO.getTimeChronos());
        }
        if(serviceEditDTO.getDeadline() != null) {
            service.setDeadline(serviceEditDTO.getDeadline());
        }
        if(serviceEditDTO.getModality() != null) {
            service.setModality(serviceEditDTO.getModality());
        }
        if(serviceEditDTO.getCategoryEntities() != null) {
            service.setCategoryEntities(serviceEditDTO.getCategoryEntities());
        }
        if (serviceEditDTO.getServiceImage() != null) {
            String jwtToken = tokenHeader.substring(7);
            String imageUrl = storageService.uploadBase64Image(serviceEditDTO.getServiceImage(), "services", jwtToken);
            service.setServiceImageUrl(imageUrl);
        }

        String notificationMessage = "Pedido editado";
        notificationService.create(notificationMessage, userEntity, service);

        return serviceRepository.save(service);
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

    public ServiceEntity acceptService(Long id, String tokenHeader) {
        UserEntity userAccepted = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = changeStatus(id, ServiceStatus.ACEITO);
        service.setUserAccepted(userAccepted);
        service.setVerificationCode(generateVerificationCode());

        notificationService.create("Pedido aceito", userAccepted, service);
        notificationService.create("Pedido aceito por " + userAccepted.getName(), service.getUserCreator(), service);

        return serviceRepository.save(service);
    }

    public ServiceEntity startService(Long id, String tokenHeader, String verificationCode) {
        UserEntity userAccepted = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getById(id);
        if (!Objects.equals(verificationCode, service.getVerificationCode())) {
            throw new IncorrectValidationCodeException("Código de verificação incorreto");
        }

        service = changeStatus(id, ServiceStatus.EM_ANDAMENTO);
        notificationService.create("Pedido iniciado", userAccepted, service);
        notificationService.create("Pedido iniciado", service.getUserCreator(), service);
        return serviceRepository.save(service);
    }

    public ServiceEntity finishService(Long id, String tokenHeader) {
        userService.getLoggedUser(tokenHeader);
        ServiceEntity service = changeStatus(id, ServiceStatus.CONCLUIDO);

        notificationService.create("Pedido finalizado", service.getUserCreator(), service);
        notificationService.create("Pedido finalizado", service.getUserAccepted(), service);
        return serviceRepository.save(service);
    }

    public ServiceEntity cancelService(Long id, String tokenHeader) {
        UserEntity user = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getById(id);

        if (user == service.getUserCreator()) {
            service = changeStatus(id, ServiceStatus.CANCELADO);
            notificationService.create("Pedido cancelado", user, service);
            if (service.getUserAccepted() != null) {
                notificationService.create("Pedido cancelado por " + user, service.getUserAccepted(), service);
            }
        } else {
            service.setUserAccepted(null);
            service = changeStatus(id, ServiceStatus.CRIADO);
            notificationService.create("Pedido cancelado", user, service);
            notificationService.create("Pedido cancelado por " + user, service.getUserCreator(), service);
        }
        return serviceRepository.save(service);
    }

    public ServiceEntity getById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Serviço com ID " + id + " não encontrado."));
    }

    @Transactional
    public List<ServiceEntity> getAll(String tokenHeader) {
        try {
            userService.getLoggedUser(tokenHeader);
            return serviceRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace(); // Isso vai mostrar o erro REAL no console
            throw new AuthException("Erro interno: " + e.getMessage());
        }
    }

    @Transactional
    public List<ServiceEntity> getAllByStatus(ServiceStatus status, String tokenHeader) {
        try {
            userService.getLoggedUser(tokenHeader);
            return serviceRepository.findAllByStatus(status);
        } catch (Exception e) {
            e.printStackTrace(); // Isso vai mostrar o erro REAL no console
            throw new AuthException("Erro interno: " + e.getMessage());
        }
    }
}
