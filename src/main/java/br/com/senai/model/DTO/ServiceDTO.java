package br.com.senai.model.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    @NotBlank(message = "Título do serviço é obrigatório")
    private String title;

    @NotBlank(message = "Descrição do serviço é obrigatória")
    private String description;

    @NotNull(message = "Tempo em Chronos do serviço é obrigatório")
    private Integer timeChronos;

    @NotNull(message = "Modalidade do serviço é obrigatório")
    private String modality;

    @NotNull(message = "Prazo do serviço é obrigatório")
    private LocalDate deadline;

    @NotBlank(message = "Categoria do serviço é obrigatória")
    private List<String> categories;

    @NotBlank(message = "Imagem de serviço é obrigatória")
    private String serviceImage;
}
