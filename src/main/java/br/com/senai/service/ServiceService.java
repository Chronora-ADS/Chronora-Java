package br.com.senai.service;

import br.com.senai.enums.ServiceStatus;
import br.com.senai.exception.Auth.AuthException;
import br.com.senai.exception.NotFound.ServiceNotFoundException;
import br.com.senai.exception.NotFound.UserNotFoundException;
import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.ServiceRepository;
import br.com.senai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public ServiceEntity create(ServiceDTO serviceDTO, Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + userId + " não encontrado."));

        ServiceEntity service = new ServiceEntity();
        service.setTitle(serviceDTO.getTitle());
        service.setDescription(serviceDTO.getDescription());
        service.setTimeChronos(serviceDTO.getTimeChronos());
        service.setDeadline(serviceDTO.getDeadline());
        service.setCategoryEntities(serviceDTO.getCategoryEntities());
        service.setServiceLocation(serviceDTO.getServiceLocation());
        service.setStatus(ServiceStatus.CRIADO);

        // Decodifica o Base64
        String[] partes = serviceDTO.getServiceImage().split(",");
        String dadosBase64 = (partes.length > 1) ? partes[1] : partes[0];
        service.setServiceImage(Base64.getDecoder().decode(dadosBase64));
        service.setCreatorUser(userEntity);

        return serviceRepository.save(service);
    }

    public ServiceEntity getById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Serviço com ID " + id + " não encontrado."));
    }

    public List<ServiceEntity> getAllCreated() {
        return serviceRepository.findAllByStatus(ServiceStatus.CRIADO);
    }

    public List<ServiceEntity> getAllInitialized() {
        return serviceRepository.findAllByStatus(ServiceStatus.INICIADO);
    }

    public List<ServiceEntity> getAllAccepted() {
        return serviceRepository.findAllByStatus(ServiceStatus.ACEITO);
    }

    public List<ServiceEntity> getAllCancelled() {
        return serviceRepository.findAllByStatus(ServiceStatus.CANCELADO);
    }

    public List<ServiceEntity> getAllFinalized() {
        return serviceRepository.findAllByStatus(ServiceStatus.FINALIZADO);
    }

    public List<ServiceEntity> getAll() {
        return serviceRepository.findAll();
    }

    public ServiceEntity updateToAcceptedStatus(Long serviceId, ServiceStatus newStatus, Long userId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException("Serviço com ID " + serviceId + " não encontrado."));
        if (newStatus == ServiceStatus.CRIADO && !canUserUpdateStatusCreated(service, userId)) {
            throw new AuthException("Você não possui permissão para aceitar este serviço.");
        }

        service.setStatus(newStatus);
        return serviceRepository.save(service);
    }

    private boolean canUserUpdateStatusCreated(ServiceEntity service, Long userId) {
        Long creatorId = service.getCreatorUser().getId();
        Long acceptedById = service.getAcceptedByUser() != null ? service.getAcceptedByUser().getId() : null;
        ServiceStatus status = service.getStatus();
        return !userId.equals(creatorId) && acceptedById == null && status == ServiceStatus.CRIADO;
    }
}
