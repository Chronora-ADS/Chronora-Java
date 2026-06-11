package br.com.senai.model.DTO.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank(message = "E-mail de login é obrigatório.")
    @Email(message = "E-mail de login é inválido.")
    private String email;

    @NotBlank(message = "Senha de login é obrigatória.")
    private String password;
}
