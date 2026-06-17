package br.com.senai.repository;

import br.com.senai.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findBySupabaseUserId(String supabaseUserId);

    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPhoneNumber(Long phoneNumber);

    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(Long phoneNumber);

    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.roles ORDER BY u.name ASC")
    List<UserEntity> findAllWithRoles();
}
