package com.example.client_server.model.DTO;

import com.example.client_server.model.entity.CategoryEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ServiceDTO {
    @NotBlank(message = "Título é obrigatório")
    private String title;

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @NotNull(message = "Tempo em Chronos é obrigatório")
    private Integer timeChronos;

    @NotBlank(message = "Categoria é obrigatória")
    private List<CategoryEntity> categoryEntities;

    @NotBlank(message = "Imagem de serviço é obrigatória")
    private String serviceImage;
}
