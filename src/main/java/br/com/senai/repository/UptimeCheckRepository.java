package br.com.senai.repository;

import br.com.senai.model.entity.UptimeCheckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UptimeCheckRepository extends JpaRepository<UptimeCheckEntity, Long> {
}
