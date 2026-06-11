package br.com.senai.model.DTO.user;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserEditDTO {
    private Long id;
    private String name;

    @Email(message = "E-mail do usuário é inválido.")
    private String email;

    private Long phoneNumber;
    private String password;
    private DocumentDTO document;
    private DocumentDTO profileImage;
}
