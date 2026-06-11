package br.com.senai.model.DTO.service;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDTO {
    @NotBlank(message = "Título do serviço é obrigatório.")
    private String title;

    @NotBlank(message = "Descrição do serviço e obrigatória.")
    @Size(max = 2500, message = "Descrição do serviço deve ter no máximo 2500 caracteres.")
    private String description;

    @NotNull(message = "Tempo em Chronos do serviço e obrigatório.")
    @Positive(message = "Tempo em Chronos do serviço deve ser maior que zero.")
    @Max(value = 100, message = "Limite de chronos de 100 por serviço excedido.")
    private Integer timeChronos;

    @NotBlank(message = "Modalidade do serviço e obrigatória.")
    private String modality;

    @NotNull(message = "Prazo do serviço e obrigatório.")
    private LocalDate deadline;

    @NotEmpty(message = "Categoria do serviço e obrigatória.")
    @Size(max = 10, message = "O serviço pode ter no máximo 10 categorias.")
    private List<@NotBlank(message = "Categoria do serviço e obrigatória.") String> categories;

    @NotBlank(message = "Imagem de serviço e obrigatória.")
    private String serviceImage;
}
