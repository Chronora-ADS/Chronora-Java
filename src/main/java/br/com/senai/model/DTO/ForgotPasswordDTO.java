package br.com.senai.model.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordDTO {
    @NotBlank
    @Email
    @JsonAlias({"email", "Email"})
    private String email;

    @JsonAlias({"redirectTo", "redirect_to"})
    private String redirectTo;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRedirectTo() {
        return redirectTo;
    }

    public void setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
    }
}
