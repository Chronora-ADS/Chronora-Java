package br.com.senai.model.DTO.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupabaseUserDTO {
    private String id;
    private String email;
    private String phone;
    private String createdAt;

    // TODO ver para que serve esse builder
    public static Builder builder() {
        return new Builder();
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
