package br.com.senai.model.DTO.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
    @NotBlank(message = "Nome do usuário é obrigatório.")
    @JsonAlias("Name")
    private String name;

    @NotBlank(message = "E-mail do usuário é obrigatório.")
    @Email(message = "E-mail do usuário é inválido.")
    @JsonAlias("Email")
    private String email;

    @NotNull(message = "Número de telefone do usuário é obrigatório.")
    @JsonAlias("PhoneNumber")
    private Long phoneNumber;

    @NotBlank(message = "Senha do usuário é obrigatória.")
    @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.")
    @JsonAlias("Password")
    private String password;

    @NotNull(message = "Documento do usuário é obrigatório.")
    @Valid
    @JsonAlias("Document")
    private DocumentDTO document;
}
