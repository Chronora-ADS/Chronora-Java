package br.com.senai.model.DTO;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ServiceDeadlineRenewalDTO {
    @NotNull(message = "Novo prazo do servico e obrigatorio")
    private LocalDate deadline;

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
}
