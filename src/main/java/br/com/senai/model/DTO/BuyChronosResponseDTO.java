package br.com.senai.model.DTO;

public record BuyChronosResponseDTO(
        Long transactionId,
        String qrCode,
        String qrCodeBase64,
        String expiresAt
) {}
