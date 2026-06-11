package br.com.senai.model.DTO.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupabaseAuthResponseDTO {
    private SupabaseUserDTO user;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;

    // TODO ver para que serve esse builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SupabaseUserDTO user;
        private String accessToken;
        private String refreshToken;
        private Long expiresIn;

        public Builder user(SupabaseUserDTO user) {
            this.user = user;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public SupabaseAuthResponseDTO build() {
            return new SupabaseAuthResponseDTO(user, accessToken, refreshToken, expiresIn);
        }
    }
}
