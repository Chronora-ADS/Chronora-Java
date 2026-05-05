package br.com.senai.model.entity;

import jakarta.persistence.*;
@Embeddable
public class DocumentEntity {
    @Column(name = "document_name", nullable = false)
    private String name;

    @Column(name = "document_type", nullable = false)
    private String type;

    @Column(name = "document_url", nullable = false, length = 500)
    private String url;

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
