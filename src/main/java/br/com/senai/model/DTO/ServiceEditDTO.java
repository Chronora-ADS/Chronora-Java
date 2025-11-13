package br.com.senai.model.DTO;

import br.com.senai.model.entity.CategoryEntity;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ServiceEditDTO {
    private Long id;
    private String title;
    private String description;
    private Integer timeChronos;
    private String modality;
    private LocalDate deadline;
    private List<CategoryEntity> categoryEntities;
    private String serviceImage;
}