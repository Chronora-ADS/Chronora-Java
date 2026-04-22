package br.com.senai.model.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordDTO {
    @NotBlank
    @Size(min = 6, max = 72, message = "A nova senha deve ter entre 6 e 72 caracteres")
    @JsonAlias({"newPassword", "password"})
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
