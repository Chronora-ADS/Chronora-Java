package br.com.senai.model.DTO.payment;

import java.time.LocalDateTime;

public record ChronosExtractItemDTO(
        String type,
        int chronosAmount,
        LocalDateTime date,
        String description,
        String status
) {}
