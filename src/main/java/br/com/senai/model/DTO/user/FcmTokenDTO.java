package br.com.senai.model.DTO.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FcmTokenDTO {

    @NotBlank(message = "O token FCM é obrigatório.")
    private String token;
}
