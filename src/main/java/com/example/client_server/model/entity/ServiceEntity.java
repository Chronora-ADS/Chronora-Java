package com.example.client_server.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "service")
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2500)
    private String description;

    @Column(nullable = false)
    private Integer timeChronos;

    @ElementCollection
    private List<CategoryEntity> categoryEntities;

    @Lob
    @Column(name = "service_image", nullable = false, columnDefinition = "bytea")
    private byte[] serviceImage;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private UserEntity userEntity;
}
