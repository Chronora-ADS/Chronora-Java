package br.com.senai.repository;

import br.com.senai.model.entity.PaymentTransactionEntity;
import br.com.senai.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {

    Optional<PaymentTransactionEntity> findByMpPaymentId(Long mpPaymentId);

    Optional<PaymentTransactionEntity> findByIdAndUserId(Long id, Long userId);

    boolean existsByMpPaymentIdAndStatus(Long mpPaymentId, PaymentStatus status);
}
