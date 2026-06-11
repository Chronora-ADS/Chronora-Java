package br.com.senai.model.DTO.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordDTO {
    @NotBlank
    @Email
    @JsonAlias({"email", "Email"})
    private String email;

    @JsonAlias({"redirectTo", "redirect_to"})
    private String redirectTo;
}
