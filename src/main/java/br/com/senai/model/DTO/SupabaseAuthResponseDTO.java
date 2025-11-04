package br.com.senai.model.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupabaseAuthResponseDTO {
    private SupabaseUserDTO user;
    private String accessToken;
    private String refreshToken;
}
