package br.com.senai.repository;

import br.com.senai.model.entity.ReviewEntity;
import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    boolean existsByServiceAndReviewer(ServiceEntity service, UserEntity reviewer);

    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.reviewee.id = :userId")
    Double calculateAverageRatingForUser(@Param("userId") Long userId);
}
