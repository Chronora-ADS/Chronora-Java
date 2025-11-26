package br.com.senai.service;

import br.com.senai.model.DTO.RequestDTO;
import br.com.senai.model.DTO.ServiceDTO;
import br.com.senai.model.entity.CategoryEntity;
import br.com.senai.model.entity.ServiceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final ServiceService serviceService;

    public ServiceEntity createFromRequest(RequestDTO requestDTO, String tokenHeader) {
        // Converter RequestDTO para ServiceDTO
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setTitle(requestDTO.getTitle());
        serviceDTO.setDescription(requestDTO.getDescription());
        serviceDTO.setTimeChronos(requestDTO.getTimeChronos());
        serviceDTO.setModality(requestDTO.getModality());
        serviceDTO.setDeadline(requestDTO.getDeadline());

        // Converter categorias de String para CategoryEntity
        List<CategoryEntity> categoryEntities = requestDTO.getCategories().stream()
                .map(categoryName -> {
                    CategoryEntity category = new CategoryEntity();
                    category.setName(categoryName);
                    return category;
                })
                .collect(Collectors.toList());
        serviceDTO.setCategoryEntities(categoryEntities);

        // Converter requestImage para serviceImage
        serviceDTO.setServiceImage(requestDTO.getRequestImage());

        return serviceService.create(serviceDTO, tokenHeader);
    }
}