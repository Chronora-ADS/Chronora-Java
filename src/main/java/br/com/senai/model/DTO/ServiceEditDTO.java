package br.com.senai.model.DTO;

import br.com.senai.model.entity.CategoryEntity;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class ServiceEditDTO {
    private Long id;
    private String title;
    @Size(max = 2500, message = "Descricao do servico deve ter no maximo 2500 caracteres")
    private String description;
    @Positive(message = "Tempo em Chronos do servico deve ser maior que zero")
    @Max(value = 100, message = "Limite de chronos de 100 por servico excedido")
    private Integer timeChronos;
    private String modality;
    private LocalDate deadline;
    @JsonAlias("categories")
    @Size(max = 10, message = "O servico pode ter no maximo 10 categorias")
    private List<@jakarta.validation.constraints.NotBlank(message = "Categoria do servico e obrigatoria") String> categories;
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

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getServiceImage() {
        return serviceImage;
    }

    public void setServiceImage(String serviceImage) {
        this.serviceImage = serviceImage;
    }
}
