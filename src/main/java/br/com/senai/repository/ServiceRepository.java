package br.com.senai.repository;

import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.enums.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    List<ServiceEntity> findAllByStatus(ServiceStatus status);
}
