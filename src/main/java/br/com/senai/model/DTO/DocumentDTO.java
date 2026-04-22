package br.com.senai.model.DTO;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonAlias;
public class DocumentDTO {
    @NotBlank(message = "Nome do documento é obrigatório")
    @JsonAlias("Name")
    private String name;

    @NotBlank(message = "Tipo do documento é obrigatório")
    @JsonAlias("Type")
    private String type;

    @NotBlank(message = "Documento é obrigatório")
    @JsonAlias({"Data", "base64"})
    private String data; // Base64 string

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
