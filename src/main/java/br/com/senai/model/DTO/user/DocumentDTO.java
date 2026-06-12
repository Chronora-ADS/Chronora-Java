package br.com.senai.model.DTO.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentDTO {
    @NotBlank(message = "Nome do documento é obrigatório.")
    @JsonAlias("Name")
    private String name;

    @NotBlank(message = "Tipo do documento é obrigatório.")
    @JsonAlias("Type")
    private String type;

    @NotBlank(message = "Documento é obrigatório.")
    @JsonAlias({"Data", "base64"})
    private String data;
}
