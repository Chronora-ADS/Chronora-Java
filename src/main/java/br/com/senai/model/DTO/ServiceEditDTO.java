package br.com.senai.model.DTO;

import br.com.senai.model.entity.CategoryEntity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public class ServiceEditDTO {
    private Long id;
    private String title;
    private String description;
    @Positive(message = "Tempo em Chronos do servico deve ser maior que zero")
    @Max(value = 100, message = "Limite de chronos de 100 por servico excedido")
    private Integer timeChronos;
    private String modality;
    private LocalDate deadline;
    private List<CategoryEntity> categoryEntities;
    private String serviceImage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTimeChronos() {
        return timeChronos;
    }

    public void setTimeChronos(Integer timeChronos) {
        this.timeChronos = timeChronos;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public List<CategoryEntity> getCategoryEntities() {
        return categoryEntities;
    }

    public void setCategoryEntities(List<CategoryEntity> categoryEntities) {
        this.categoryEntities = categoryEntities;
    }

    public String getServiceImage() {
        return serviceImage;
    }

    public void setServiceImage(String serviceImage) {
        this.serviceImage = serviceImage;
    }
}
