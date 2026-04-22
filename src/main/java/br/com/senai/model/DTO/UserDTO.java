package br.com.senai.model.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserDTO {
    @NotBlank(message = "Nome do usuario e obrigatorio")
    @JsonAlias("Name")
    private String name;

    @NotBlank(message = "Email do usuario e obrigatorio")
    @Email(message = "Email do usuario e invalido")
    @JsonAlias("Email")
    private String email;

    @NotNull(message = "Numero de telefone do usuario e obrigatorio")
    @JsonAlias("PhoneNumber")
    private Long phoneNumber;

    @NotBlank(message = "Senha do usuario e obrigatoria")
    @JsonAlias("Password")
    private String password;

    @NotNull(message = "Documento do usuario e obrigatorio")
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
