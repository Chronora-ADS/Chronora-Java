package br.com.senai.model.DTO;

import br.com.senai.model.entity.CategoryEntity;
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
public class RequestDTO {
    @NotBlank(message = "Título do pedido é obrigatório")
    private String title;

    @NotBlank(message = "Descrição do pedido é obrigatória")
    private String description;

    @NotNull(message = "Tempo em Chronos do pedido é obrigatório")
    private Integer timeChronos;

    @NotNull(message = "Modalidade do pedido é obrigatória")
    private String modality;

    @NotNull(message = "Prazo do pedido é obrigatório")
    private LocalDate deadline;

    @NotNull(message = "Categorias do pedido são obrigatórias")
    private List<String> categories;

    private String requestImage;
}