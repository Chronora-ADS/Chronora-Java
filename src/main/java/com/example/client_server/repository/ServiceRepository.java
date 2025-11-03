package com.example.client_server.repository;

import com.example.client_server.model.entity.ServiceEntity;
import com.example.client_server.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
}