package br.com.senai.model.DTO.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupabaseAuthResponseDTO {
    private SupabaseUserDTO user;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}
