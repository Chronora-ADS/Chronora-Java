package br.com.senai.model.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class ServiceDTO {
    @NotBlank(message = "Titulo do servico e obrigatorio")
    private String title;

    @NotBlank(message = "Descricao do servico e obrigatoria")
    @Size(max = 2500, message = "Descricao do servico deve ter no maximo 2500 caracteres")
    private String description;

    @NotNull(message = "Tempo em Chronos do servico e obrigatorio")
    @Positive(message = "Tempo em Chronos do servico deve ser maior que zero")
    @Max(value = 100, message = "Limite de chronos de 100 por servico excedido")
    private Integer timeChronos;

    @NotBlank(message = "Modalidade do servico e obrigatoria")
    private String modality;

    @NotNull(message = "Prazo do servico e obrigatorio")
    private LocalDate deadline;

    @NotEmpty(message = "Categoria do servico e obrigatoria")
    @Size(max = 10, message = "O servico pode ter no maximo 10 categorias")
    private List<@NotBlank(message = "Categoria do servico e obrigatoria") String> categories;

    @NotBlank(message = "Imagem de servico e obrigatoria")
    private String serviceImage;

    public ServiceDTO() {
    }

    public ServiceDTO(
            String title,
            String description,
            Integer timeChronos,
            String modality,
            LocalDate deadline,
            List<String> categories,
            String serviceImage
    ) {
        this.title = title;
        this.description = description;
        this.timeChronos = timeChronos;
        this.modality = modality;
        this.deadline = deadline;
        this.categories = categories;
        this.serviceImage = serviceImage;
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
