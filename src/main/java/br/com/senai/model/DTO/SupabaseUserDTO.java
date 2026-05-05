package br.com.senai.model.DTO;

public class SupabaseUserDTO {
    private String id;
    private String email;
    private String phone;
    private String createdAt;

    public SupabaseUserDTO() {
    }

    public SupabaseUserDTO(String id, String email, String phone, String createdAt) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

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

    public static class Builder {
        private String id;
        private String email;
        private String phone;
        private String createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder createdAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SupabaseUserDTO build() {
            return new SupabaseUserDTO(id, email, phone, createdAt);
        }
    }
}
