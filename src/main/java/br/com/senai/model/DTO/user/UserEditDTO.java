package br.com.senai.model.DTO.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserEditDTO {
    private Long id;
    private String name;

    @Email(message = "E-mail do usuário é inválido.")
    private String email;

    private Long phoneNumber;

    @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.")
    private String password;
    private DocumentDTO document;
    private DocumentDTO profileImage;
}
