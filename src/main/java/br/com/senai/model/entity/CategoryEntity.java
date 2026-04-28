package br.com.senai.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.Data;

@Embeddable
@Data
public class CategoryEntity {
    @Column(name = "category_name", nullable = false)
    private String name;
    // Explicit getters/setters to avoid IDE issues when Lombok isn't processed
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}