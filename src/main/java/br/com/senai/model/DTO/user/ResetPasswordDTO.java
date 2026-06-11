package br.com.senai.model.DTO.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotBlank
    // TODO GABRIEL verificar se na criação de um usuário e no editar também está com a mesma regra de senha, tanto no backend quanto no frontend
    //  Padronizados no backend conforme no front, só falta na página de perfil
    @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres.")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "A senha deve conter pelo menos uma letra maiúscula e um número.")
    @JsonAlias({"newPassword", "password"})
    private String newPassword;
}
