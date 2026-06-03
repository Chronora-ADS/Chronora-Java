package br.com.senai.repository;

import br.com.senai.model.entity.PaymentTransactionEntity;
import br.com.senai.model.enums.PaymentStatus;
import br.com.senai.model.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {

    Optional<PaymentTransactionEntity> findByMpPaymentId(Long mpPaymentId);

    Optional<PaymentTransactionEntity> findByIdAndUserId(Long id, Long userId);

    boolean existsByMpPaymentIdAndStatus(Long mpPaymentId, PaymentStatus status);

    @Query("SELECT t FROM PaymentTransactionEntity t WHERE t.userId = :userId AND t.type = :type AND t.status = :status AND t.expiresAt > :now ORDER BY t.createdAt DESC")
    Optional<PaymentTransactionEntity> findFirstActiveTransaction(
            @Param("userId") Long userId,
            @Param("type") PaymentType type,
            @Param("status") PaymentStatus status,
            @Param("now") LocalDateTime now
    );
}
