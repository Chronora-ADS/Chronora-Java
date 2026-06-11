package br.com.senai.model.entity;

import br.com.senai.model.enums.ServiceModality;
import br.com.senai.model.enums.ServiceStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
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

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private ServiceModality modality;

    @Column(nullable = false)
    private LocalDateTime postedAt;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private ServiceStatus status;

    private String verificationCode;

    private LocalDateTime verificationCodeExpiresAt;

    // TODO ISAIAS novamente, para que serve o call count do código de verificação?
    @Column(name = "verification_code_call_count")
    private Integer verificationCodeCallCount = 0;

    // TODO ISAIAS justificativa e quem justificou dentro do serviço é desnecessária, levando em consideração que criar as notificações já é o suficiente
    @Column(name = "service_cancellation_justification", length = 1000)
    private String serviceCancellationJustification;

    @Column(name = "service_cancellation_requested_by_user_id")
    private Long serviceCancellationRequestedByUserId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "service_entity_category_entities",
            joinColumns = @JoinColumn(name = "service_entity_id")
    )
    @Column(name = "category_name")
    private List<CategoryEntity> categoryEntities;

    public List<String> getCategories() {
        if (categoryEntities == null) {
            return List.of();
        }
        return categoryEntities.stream()
                .map(CategoryEntity::getName)
                .toList();
    }

    public void setCategories(List<String> categories) {
        if (categories == null) {
            this.categoryEntities = null;
            return;
        }
        this.categoryEntities = categories.stream()
                .map(category -> {
                    CategoryEntity categoryEntity = new CategoryEntity();
                    categoryEntity.setName(category);
                    return categoryEntity;
                })
                .toList();
    }

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

    @Column(name = "rated_by_creator", nullable = false)
    private boolean ratedByCreator = false;

    @Column(name = "rated_by_provider", nullable = false)
    private boolean ratedByProvider = false;
}
