package com.example.client_server.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.Data;

@Embeddable
@Data
public class CategoryEntity {
    @Column(name = "category_name", nullable = false)
    private String name;
}

