package br.com.senai.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private LocalDate deadline;

    @Column(nullable = false)
    private String modality;

    @Column(nullable = false)
    private LocalDateTime postedAt;

    @ElementCollection
    private List<CategoryEntity> categoryEntities;

    @Lob
    @Column(name = "service_image", nullable = false)
    @JsonIgnore
    private byte[] serviceImage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_creator_id", nullable = false)
    @JsonIgnoreProperties({"password", "roles", "documentEntity", "supabaseUserId"})
    private UserEntity userCreator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_accepted_id")
    @JsonIgnoreProperties({"password", "roles", "documentEntity", "supabaseUserId"})
    private UserEntity userAccepted;
}
