package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.ServiceNotFoundException;
import br.com.senai.exception.Validation.QuantityChronosInvalidException;
import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.DTO.ServiceEditDTO;
import br.com.senai.model.entity.CategoryEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.ServiceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final UserService userService;
    private final SupabaseStorageService storageService;

    public ServiceService(
            ServiceRepository serviceRepository,
            UserService userService,
            SupabaseStorageService storageService
    ) {
        this.serviceRepository = serviceRepository;
        this.userService = userService;
        this.storageService = storageService;
    }

    public ServiceEntity create(ServiceDTO serviceDTO, String tokenHeader) {
        UserEntity userEntity = userService.getLoggedUser(tokenHeader);
        validateServiceChronos(serviceDTO.getTimeChronos());

        ServiceEntity service = new ServiceEntity();
        service.setTitle(serviceDTO.getTitle());
        service.setDescription(serviceDTO.getDescription());
        service.setTimeChronos(serviceDTO.getTimeChronos());
        service.setDeadline(serviceDTO.getDeadline());
        service.setModality(serviceDTO.getModality());
        service.setPostedAt(LocalDateTime.now());
        service.setCategoryEntities(buildCategories(serviceDTO.getCategories()));
        service.setUserCreator(userEntity);

        if (serviceDTO.getServiceImage() != null && !serviceDTO.getServiceImage().isEmpty()) {
            String jwtToken = tokenHeader.substring(7);
            String imageUrl = storageService.uploadBase64Image(serviceDTO.getServiceImage(), "services", jwtToken);
            service.setServiceImageUrl(imageUrl);
        }

        return serviceRepository.save(service);
    }

    public ServiceEntity put(ServiceEditDTO serviceEditDTO, String tokenHeader) {
        UserEntity userEntity = userService.getLoggedUser(tokenHeader);
        ServiceEntity service = getById(serviceEditDTO.getId());

        if (!Objects.equals(service.getUserCreator().getId(), userEntity.getId())) {
            throw new AuthException("Credenciais inválidas.");
        }
        if (serviceEditDTO.getTimeChronos() != null) {
            validateServiceChronos(serviceEditDTO.getTimeChronos());
        }

        if (serviceEditDTO.getTitle() != null) {
            service.setTitle(serviceEditDTO.getTitle());
        }
        if (serviceEditDTO.getDescription() != null) {
            service.setDescription(serviceEditDTO.getDescription());
        }
        if (serviceEditDTO.getTimeChronos() != null) {
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
        if (serviceEditDTO.getServiceImage() != null && !serviceEditDTO.getServiceImage().isEmpty()) {
            String jwtToken = tokenHeader.substring(7);
            String imageUrl = storageService.uploadBase64Image(serviceEditDTO.getServiceImage(), "services", jwtToken);
            service.setServiceImageUrl(imageUrl);
        }

        return serviceRepository.save(service);
    }

    public ServiceEntity getById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Serviço com ID " + id + " não encontrado."));
    }

    @Transactional
    public List<ServiceEntity> getAll(String tokenHeader) {
        userService.getLoggedUser(tokenHeader);
        return serviceRepository.findAll();
    }

    private List<CategoryEntity> buildCategories(List<String> categories) {
        List<CategoryEntity> categoryEntities = new ArrayList<>();
        for (String category : categories) {
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setName(category);
            categoryEntities.add(categoryEntity);
        }
        return categoryEntities;
    }

    private void validateServiceChronos(Integer timeChronos) {
        if (timeChronos == null || timeChronos <= 0) {
            throw new QuantityChronosInvalidException("A quantidade de chronos do serviço deve ser maior que zero.");
        }
        if (timeChronos > 100) {
            throw new QuantityChronosInvalidException("Limite de chronos de 100 por serviço excedido.");
        }
    }
}
