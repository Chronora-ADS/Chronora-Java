package br.com.senai.model.entity;

import br.com.senai.model.enums.ServiceModality;
import br.com.senai.model.enums.ServiceStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Entity
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceModality modality;

    @Column(nullable = false)
    private LocalDateTime postedAt;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private ServiceStatus status;

    private String verificationCode;

    private LocalDateTime verificationCodeExpiresAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "service_entity_category_entities",
            joinColumns = @JoinColumn(name = "service_entity_id")
    )
    @Column(name = "category_name")
    private List<CategoryEntity> categoryEntities;

    @Column(length = 500)
    private String serviceImageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_creator_id", nullable = false)
    @JsonIgnoreProperties({"password", "roles", "documentEntity", "supabaseUserId"})
    private UserEntity userCreator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_accepted_id")
    @JsonIgnoreProperties({"password", "roles", "documentEntity", "supabaseUserId"})
    private UserEntity userAccepted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTimeChronos() {
        return timeChronos;
    }

    public void setTimeChronos(Integer timeChronos) {
        this.timeChronos = timeChronos;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public ServiceModality getModality() {
        return modality;
    }

    public void setModality(ServiceModality modality) {
        this.modality = modality;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = postedAt;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public LocalDateTime getVerificationCodeExpiresAt() {
        return verificationCodeExpiresAt;
    }

    public void setVerificationCodeExpiresAt(LocalDateTime verificationCodeExpiresAt) {
        this.verificationCodeExpiresAt = verificationCodeExpiresAt;
    }

    @JsonProperty("categoryEntities")
    public List<CategoryEntity> getCategoryEntities() {
        if (categoryEntities == null) {
            return Collections.emptyList();
        }
        return categoryEntities;
    }

    public void setCategoryEntities(List<CategoryEntity> categoryEntities) {
        this.categoryEntities = categoryEntities;
    }

    @JsonProperty("categories")
    public List<String> getCategories() {
        if (categoryEntities == null) {
            return Collections.emptyList();
        }

        return categoryEntities.stream()
                .map(CategoryEntity::getName)
                .toList();
    }

    public String getServiceImageUrl() {
        return serviceImageUrl;
    }

    public void setServiceImageUrl(String serviceImageUrl) {
        this.serviceImageUrl = serviceImageUrl;
    }

    @JsonProperty("serviceImage")
    public String getServiceImage() {
        return serviceImageUrl;
    }

    public UserEntity getUserCreator() {
        return userCreator;
    }

    public void setUserCreator(UserEntity userCreator) {
        this.userCreator = userCreator;
    }

    public UserEntity getUserAccepted() {
        return userAccepted;
    }

    public void setUserAccepted(UserEntity userAccepted) {
        this.userAccepted = userAccepted;
    }
}
