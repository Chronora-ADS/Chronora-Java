package br.com.senai.model.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ServiceCancellationDTO {
    @NotBlank(message = "Justificativa do cancelamento e obrigatoria")
    @Size(max = 1000, message = "Justificativa do cancelamento deve ter no maximo 1000 caracteres")
    private String justification;

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }
}
