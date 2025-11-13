package br.com.senai.service;

import br.com.senai.exception.NotFound.ServiceNotFoundException;
import br.com.senai.exception.NotFound.UserNotFoundException;
import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.ServiceRepository;
import br.com.senai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

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
        service.setDeadline(LocalTime.now());
        service.setModality(serviceDTO.getModality());
        service.setCategoryEntities(serviceDTO.getCategoryEntities());

        // Decodifica o Base64
        String[] partes = serviceDTO.getServiceImage().split(",");
        String dadosBase64 = (partes.length > 1) ? partes[1] : partes[0];
        service.setServiceImage(Base64.getDecoder().decode(dadosBase64));
        service.setUserEntity(userEntity);

        return serviceRepository.save(service);
    }

    public ServiceEntity getById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Serviço com ID " + id + " não encontrado."));
    }

    public List<ServiceEntity> getAll() {
        return serviceRepository.findAll();
    }
}
