package br.com.senai.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
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
    private Date deadline;

    @ElementCollection
    private List<CategoryEntity> categoryEntities;

    // Presencial e Home Office
    @Column(nullable = false)
    private String serviceLocation;

    @Lob
    @Column(name = "service_image", nullable = false, columnDefinition = "bytea")
    private byte[] serviceImage;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private String status;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private UserEntity creatorUser;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "accepted_by_user_id")
    private UserEntity acceptedByUser;
}
