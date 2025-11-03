package com.example.client_server.service;

import com.example.client_server.model.DTO.ServiceDTO;
import com.example.client_server.model.entity.ServiceEntity;
import com.example.client_server.model.entity.UserEntity;
import com.example.client_server.repository.ServiceRepository;
import com.example.client_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public ServiceEntity create(ServiceDTO serviceDTO, Long userId) throws Exception {
        Optional<UserEntity> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new Exception("Usuário não encontrado");
        }

        UserEntity userEntity = userOptional.get();

        ServiceEntity service = new ServiceEntity();
        service.setTitle(serviceDTO.getTitle());
        service.setDescription(serviceDTO.getDescription());
        service.setTimeChronos(serviceDTO.getTimeChronos());
        service.setCategoryEntities(serviceDTO.getCategoryEntities());

        // Decodifica o Base64
        String[] partes = serviceDTO.getServiceImage().split(",");
        String dadosBase64 = (partes.length > 1) ? partes[1] : partes[0];
        service.setServiceImage(Base64.getDecoder().decode(dadosBase64));
        service.setUserEntity(userEntity);

        return serviceRepository.save(service);
    }

    public Optional<ServiceEntity> getById(Long id) {
        return serviceRepository.findById(id);
    }

    public List<ServiceEntity> getAll() {
        return serviceRepository.findAll();
    }
}
