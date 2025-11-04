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

    @Column(name = "document_data", nullable = false, columnDefinition = "bytea")
    private byte[] data;
}
