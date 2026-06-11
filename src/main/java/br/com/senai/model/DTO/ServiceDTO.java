package br.com.senai.model.DTO;

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
}
