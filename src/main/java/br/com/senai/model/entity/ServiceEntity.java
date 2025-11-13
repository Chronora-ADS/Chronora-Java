package br.com.senai.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
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

    @Column(nullable = false)
    private LocalTime deadline;

    @Column(nullable = false)
    private String modality;

    @ElementCollection
    private List<CategoryEntity> categoryEntities;

    @Lob
    @Column(name = "service_image", nullable = false, columnDefinition = "bytea")
    private byte[] serviceImage;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private UserEntity userEntity;
}
