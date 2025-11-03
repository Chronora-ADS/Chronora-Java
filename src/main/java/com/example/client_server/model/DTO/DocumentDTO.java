package com.example.client_server.model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentDTO {
    @NotBlank private String name;
    @NotBlank private String type;
    @NotBlank private String data; // Base64 string
}
