package br.com.senai.repository;

import br.com.senai.enums.ServiceStatus;
import br.com.senai.model.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    List<ServiceEntity> findAllByStatus(ServiceStatus status);
}