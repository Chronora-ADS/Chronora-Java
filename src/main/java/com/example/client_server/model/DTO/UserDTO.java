package com.example.client_server.model.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDTO {
    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "Email é obrigatório")
    private String email;

    @NotNull(message = "Número de telefone é obrigatório")
    private Long phoneNumber;

    @NotBlank(message = "Senha é obrigatória")
    private String password;

    @NotNull(message = "Documento é obrigatório")
    @Valid
    private DocumentDTO document;
}