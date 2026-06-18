package br.com.senai.model.DTO.service;

import br.com.senai.model.entity.CategoryEntity;
import br.com.senai.model.enums.TrackingType;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ServiceEditDTO {
    private Long id;
    private String title;

    @Size(max = 2500, message = "Descrição do serviço deve ter no máximo 2500 caracteres.")
    private String description;

    @Positive(message = "Tempo em Chronos do serviço deve ser maior que zero.")
    @Max(value = 100, message = "Limite de chronos de 100 por serviço excedido.")
    private Integer timeChronos;

    private String modality;
    private LocalDate deadline;

    @JsonAlias("categories")
    @Size(max = 10, message = "O serviço pode ter no máximo 10 categorias.")
    private List<@NotBlank(message = "Categoria do serviço e obrigatória.") String> categories;

    private List<CategoryEntity> categoryEntities;
    private String serviceImage;
    private TrackingType trackingType;

    @Size(max = 500, message = "Descricao da metrica de progresso deve ter no maximo 500 caracteres.")
    private String trackingDescription;
}
