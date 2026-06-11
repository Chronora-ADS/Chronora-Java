package br.com.senai.model.DTO.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotBlank
    // TODO verificar se na criação de um usuário e no editar também está com a mesma regra de senha, tanto no backend quanto no frontend
    @Size(min = 6, max = 72, message = "A nova senha deve ter entre 6 e 72 caracteres.")
    @JsonAlias({"newPassword", "password"})
    private String newPassword;
}
