package br.com.senai.model.DTO;

import br.com.senai.enums.ServiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceChangeStatusDTO {
    @NotNull(message = "Status do serviço é obrigatório")
    private ServiceStatus status;
}
