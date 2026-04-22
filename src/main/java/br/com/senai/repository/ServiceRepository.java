package br.com.senai.repository;

import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.model.enums.ServiceStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findAllByStatus(ServiceStatus status);
    Page<ServiceEntity> findAllByStatus(ServiceStatus status, Pageable pageable);
    List<ServiceEntity> findAllByUserCreator(UserEntity userCreator);
    List<ServiceEntity> findAllByUserAccepted(UserEntity userAccepted);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ServiceEntity s WHERE s.id = :id")
    Optional<ServiceEntity> findByIdForUpdate(@Param("id") Long id);
}
