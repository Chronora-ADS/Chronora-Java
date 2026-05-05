package br.com.senai.model.DTO;

public class SupabaseAuthResponseDTO {
    private SupabaseUserDTO user;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;

    public SupabaseAuthResponseDTO() {
    }

    public SupabaseAuthResponseDTO(SupabaseUserDTO user, String accessToken, String refreshToken, Long expiresIn) {
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public static Builder builder() {
        return new Builder();
    }

    public SupabaseUserDTO getUser() {
        return user;
    }

    public void setUser(SupabaseUserDTO user) {
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
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
