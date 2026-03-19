package br.com.senai.model.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDTO {
    @NotBlank(message = "Nome do usuário é obrigatório")
    private String name;

    @NotBlank(message = "Email do usuário é obrigatório")
    private String email;

    @NotNull(message = "Número de telefone do usuário é obrigatório")
    private Long phoneNumber;

    @NotBlank(message = "Senha do usuário é obrigatória")
    private String password;

    @NotNull(message = "Documento do usuário é obrigatório")
    @Valid
    private DocumentDTO document;
    // Explicit getters/setters to avoid IDE issues when Lombok isn't processed
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DocumentDTO getDocument() {
        return document;
    }

    public void setDocument(DocumentDTO document) {
        this.document = document;
    }
}