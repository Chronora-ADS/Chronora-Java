package br.com.senai.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Embeddable
@Data
public class DocumentEntity {
    @Column(name = "document_name", nullable = false)
    private String name;

    @Column(name = "document_type", nullable = false)
    private String type;

    @Column(name = "document_url", nullable = false, length = 500)
    private String url;
}
