package br.com.senai.model.DTO.user;

import br.com.senai.model.entity.DocumentEntity;
import lombok.Data;

@Data
public class DocumentResponseDTO {
    private String name;
    private String type;
    private String url;

    public static DocumentResponseDTO fromEntity(DocumentEntity documentEntity) {
        if (documentEntity == null) {
            return null;
        }

        DocumentResponseDTO response = new DocumentResponseDTO();
        response.setName(documentEntity.getName());
        response.setType(documentEntity.getType());
        response.setUrl(documentEntity.getUrl());
        return response;
    }
}
