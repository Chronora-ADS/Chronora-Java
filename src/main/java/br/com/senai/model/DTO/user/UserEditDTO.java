package br.com.senai.model.DTO.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserEditDTO {
    private Long id;
    private String name;

    @Email(message = "E-mail do usuário é inválido.")
    private String email;

    private Long phoneNumber;

    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "A senha deve conter pelo menos uma letra maiúscula e um número.")
    private String password;
    private DocumentDTO document;
    private DocumentDTO profileImage;
}
