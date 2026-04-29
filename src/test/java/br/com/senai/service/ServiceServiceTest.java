package br.com.senai.service;

import br.com.senai.model.DTO.ServiceEditDTO;
import br.com.senai.model.entity.CategoryEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private UserService userService;

    @Mock
    private SupabaseStorageService storageService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ServiceService serviceService;

    @Test
    void putUpdatesCategoryEntitiesFromCategoriesField() {
        String tokenHeader = "Bearer token";
        UserEntity creator = new UserEntity();
        creator.setId(1L);
        creator.setTimeChronos(12);

        ServiceEntity existingService = new ServiceEntity();
        existingService.setId(10L);
        existingService.setTitle("Servico original");
        existingService.setDescription("Descricao original");
        existingService.setTimeChronos(3);
        existingService.setDeadline(LocalDate.of(2026, 5, 10));
        existingService.setModality("Remoto");
        existingService.setUserCreator(creator);
        existingService.setCategoryEntities(List.of(category("Categoria antiga")));

        ServiceEditDTO editDTO = new ServiceEditDTO();
        editDTO.setId(existingService.getId());
        editDTO.setCategories(List.of(" Categoria nova A ", "", "Categoria nova B"));

        when(userService.getLoggedUser(tokenHeader)).thenReturn(creator);
        when(serviceRepository.findById(existingService.getId())).thenReturn(Optional.of(existingService));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceEntity editedService = serviceService.put(editDTO, tokenHeader);

        assertThat(editedService.getCategoryEntities())
                .extracting(CategoryEntity::getName)
                .containsExactly("Categoria nova A", "Categoria nova B");

        ArgumentCaptor<ServiceEntity> savedService = ArgumentCaptor.forClass(ServiceEntity.class);
        verify(serviceRepository).save(savedService.capture());
        assertThat(savedService.getValue().getCategoryEntities())
                .extracting(CategoryEntity::getName)
                .containsExactly("Categoria nova A", "Categoria nova B");
        verify(notificationService).create(eq("Pedido editado"), eq(creator), eq(existingService));
    }

    private CategoryEntity category(String name) {
        CategoryEntity category = new CategoryEntity();
        category.setName(name);
        return category;
    }
}
