package br.com.senai.model.DTO;

import br.com.senai.model.entity.DocumentEntity;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
