package br.com.senai.repository;

import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.model.enums.ServiceStatus;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long>, JpaSpecificationExecutor<ServiceEntity> {
    Page<ServiceEntity> findAllByStatus(ServiceStatus status, Pageable pageable);
    List<ServiceEntity> findAllByUserCreator(UserEntity userCreator);
    List<ServiceEntity> findAllByUserAccepted(UserEntity userAccepted);
    List<ServiceEntity> findAllByStatusIs(ServiceStatus status);

    List<ServiceEntity> findAllByStatusAndDeadline(ServiceStatus status, LocalDate deadline);
    List<ServiceEntity> findAllByStatusAndDeadlineBefore(ServiceStatus status, LocalDate deadline);

    long countByStatus(ServiceStatus status);

    @Query("SELECT COALESCE(SUM(s.timeChronos), 0) FROM ServiceEntity s WHERE s.userCreator = :creator AND s.status = :status")
    int sumTimeChronosByUserCreatorAndStatus(UserEntity creator, ServiceStatus status);

    @Query("SELECT DISTINCT s FROM ServiceEntity s LEFT JOIN FETCH s.categoryEntities ORDER BY s.postedAt DESC")
    List<ServiceEntity> findAllWithCategoriesOrderByPostedAtDesc();
}
