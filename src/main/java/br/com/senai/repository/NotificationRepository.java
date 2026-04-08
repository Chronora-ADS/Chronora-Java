package br.com.senai.repository;

import br.com.senai.model.entity.NotificationEntity;
import br.com.senai.model.entity.UserEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findAllByUser(UserEntity user);
}
