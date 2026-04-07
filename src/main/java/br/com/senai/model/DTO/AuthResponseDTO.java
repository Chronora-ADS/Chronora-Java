package br.com.senai.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponseDTO {
    private String accessToken;
    private UserResponseDTO user;

    public static AuthResponseDTO of(String accessToken, UserResponseDTO user) {
        AuthResponseDTO response = new AuthResponseDTO();
        response.setAccessToken(accessToken);
        response.setUser(user);
        return response;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @JsonProperty("access_token")
    public String getAccessTokenLegacy() {
        return accessToken;
    }

    @JsonProperty("token")
    public String getToken() {
        return accessToken;
    }

    public UserResponseDTO getUser() {
        return user;
    }

    public void setUser(UserResponseDTO user) {
        this.user = user;
    }
}
