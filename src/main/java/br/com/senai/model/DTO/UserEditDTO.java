package br.com.senai.model.DTO;

import br.com.senai.model.entity.DocumentEntity;
import jakarta.validation.constraints.Email;

public class UserEditDTO {
    private Long id;
    private String name;
    @Email(message = "Email do usuário é inválido")
    private String email;
    private Long phoneNumber;
    private String password;
    private DocumentEntity document;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public DocumentEntity getDocument() {
        return document;
    }

    public void setDocument(DocumentEntity document) {
        this.document = document;
    }
}
