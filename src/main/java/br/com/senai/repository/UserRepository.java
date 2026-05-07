package br.com.senai.repository;

import br.com.senai.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByPhoneNumber(Long phoneNumber);

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findBySupabaseUserId(String supabaseUserId);

    UserEntity findAllByEmail(String email);
}
