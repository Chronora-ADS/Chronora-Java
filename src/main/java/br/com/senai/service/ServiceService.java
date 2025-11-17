package br.com.senai.service;

import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.ServiceNotFoundException;
import br.com.senai.exception.Validation.QuantityChronosInvalidException;
import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.DTO.ServiceEditDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.ServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final UserService userService;

    public ServiceEntity create(ServiceDTO serviceDTO, String tokenHeader) {
        UserEntity userEntity = userService.getLoggedUser(tokenHeader);

        if (serviceDTO.getTimeChronos() > 100) {
            throw new QuantityChronosInvalidException("Limite de chronos de 100 por serviço excedido.");
        }

        ServiceEntity service = new ServiceEntity();
        service.setTitle(serviceDTO.getTitle());
        service.setDescription(serviceDTO.getDescription());
        service.setTimeChronos(serviceDTO.getTimeChronos());
        service.setDeadline(serviceDTO.getDeadline());
        service.setModality(serviceDTO.getModality());
        service.setPostedAt(LocalDateTime.now());
        service.setCategoryEntities(serviceDTO.getCategoryEntities());

        // Decodifica o Base64
        String[] partes = serviceDTO.getServiceImage().split(",");
        String dadosBase64 = (partes.length > 1) ? partes[1] : partes[0];
        service.setServiceImage(Base64.getDecoder().decode(dadosBase64));
        service.setUserCreator(userEntity);

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
        if(serviceEditDTO.getServiceImage() != null) {
            // Decodifica o Base64
            String[] partes = serviceEditDTO.getServiceImage().split(",");
            String dadosBase64 = (partes.length > 1) ? partes[1] : partes[0];
            service.setServiceImage(Base64.getDecoder().decode(dadosBase64));
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
}
