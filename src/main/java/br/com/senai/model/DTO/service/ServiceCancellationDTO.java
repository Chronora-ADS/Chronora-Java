package br.com.senai.model.DTO.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ServiceCancellationDTO {
    @NotBlank(message = "Justificativa do cancelamento é obrigatória.")
    @Size(max = 1000, message = "Justificativa do cancelamento deve ter no máximo 1000 caracteres.")
    private String justification;
}
