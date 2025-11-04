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
}
