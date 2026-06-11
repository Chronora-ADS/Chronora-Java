package br.com.senai.model.DTO.service;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ServiceDeadlineRenewalDTO {
    @NotNull(message = "Novo prazo do servico é obrigatório.")
    private LocalDate deadline;
}
