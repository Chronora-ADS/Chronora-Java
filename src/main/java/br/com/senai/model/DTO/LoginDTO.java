package br.com.senai.model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank(message = "E-mail de login é obrigatório")
    private String email;

    @NotBlank(message = "Senha de login é obrigatório")
    private String password;
}
