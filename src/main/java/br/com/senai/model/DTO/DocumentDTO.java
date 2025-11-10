package br.com.senai.model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentDTO {
    @NotBlank(message = "Nome do documento é obrigatório")
    private String name;

    @NotBlank(message = "Tipo do documento é obrigatório")
    private String type;

    @NotBlank(message = "Documento é obrigatório")
    private String data; // Base64 string
}
