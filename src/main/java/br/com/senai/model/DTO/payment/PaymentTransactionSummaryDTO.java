package br.com.senai.model.DTO.payment;

import br.com.senai.model.entity.PaymentTransactionEntity;
import br.com.senai.model.enums.PaymentStatus;
import br.com.senai.model.enums.PaymentType;

import java.math.BigDecimal;
import java.time.ZoneOffset;

public record PaymentTransactionSummaryDTO(
        Long id,
        Long userId,
        String userName,
        PaymentType type,
        PaymentStatus status,
        Integer chronosAmount,
        BigDecimal totalAmount,
        String createdAt,
        boolean isPix
) {
    public static PaymentTransactionSummaryDTO from(PaymentTransactionEntity t, String userName) {
        return new PaymentTransactionSummaryDTO(
                t.getId(),
                t.getUserId(),
                userName,
                t.getType(),
                t.getStatus(),
                t.getChronosAmount(),
                t.getTotalAmount(),
                t.getCreatedAt().atOffset(ZoneOffset.UTC).toString(),
                t.getQrCode() != null
        );
    }
}
