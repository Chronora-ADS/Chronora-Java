package br.com.senai.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class CategoryEntity {
    @Column(name = "category_name", nullable = false)
    private String name;
}
