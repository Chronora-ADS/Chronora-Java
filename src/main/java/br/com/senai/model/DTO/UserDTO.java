package br.com.senai.model.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonAlias;

public class UserDTO {
    @NotBlank(message = "Nome do usuário é obrigatório")
    @JsonAlias("Name")
    private String name;

    @NotBlank(message = "Email do usuário é obrigatório")
    @Email(message = "Email do usuário é inválido")
    @JsonAlias("Email")
    private String email;

    @NotNull(message = "Número de telefone do usuário é obrigatório")
    @JsonAlias("PhoneNumber")
    private Long phoneNumber;

    @NotBlank(message = "Senha do usuário é obrigatória")
    @JsonAlias("Password")
    private String password;

    @NotNull(message = "Documento do usuário é obrigatório")
    @Valid
    @JsonAlias("Document")
    private DocumentDTO document;

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
