package br.com.senai.repository;

import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import br.com.senai.model.enums.ServiceStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long>, JpaSpecificationExecutor<ServiceEntity> {
    Page<ServiceEntity> findAllByStatus(ServiceStatus status, Pageable pageable);
    List<ServiceEntity> findAllByUserCreator(UserEntity userCreator);
    List<ServiceEntity> findAllByUserAccepted(UserEntity userAccepted);
    List<ServiceEntity> findAllByStatusIs(ServiceStatus status);
}
