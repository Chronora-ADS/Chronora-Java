package br.com.senai.repository;

import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
}