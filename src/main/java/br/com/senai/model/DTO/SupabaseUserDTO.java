package br.com.senai.model.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupabaseUserDTO {
    private String id;
    private String email;
    private String phone;
    private String createdAt;
    // Explicit getters/setters to avoid IDE issues when Lombok isn't processed
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}