package br.com.senai.model.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDTO {
    @NotBlank(message = "Nome do usuário é obrigatório")
    private String name;

    @NotBlank(message = "Email do usuário é obrigatório")
    private String email;

    @NotNull(message = "Número de telefone do usuário é obrigatório")
    private Long phoneNumber;

    @NotBlank(message = "Senha do usuário é obrigatória")
    private String password;

    @NotNull(message = "Documento do usuário é obrigatório")
    @Valid
    private DocumentDTO document;
}