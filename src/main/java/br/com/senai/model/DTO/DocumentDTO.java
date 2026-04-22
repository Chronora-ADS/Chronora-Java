package br.com.senai.model.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class DocumentDTO {
    @NotBlank(message = "Nome do documento e obrigatorio")
    @JsonAlias("Name")
    private String name;

    @NotBlank(message = "Tipo do documento e obrigatorio")
    @JsonAlias("Type")
    private String type;

    @NotBlank(message = "Documento e obrigatorio")
    @JsonAlias({"Data", "base64"})
    private String data;

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
