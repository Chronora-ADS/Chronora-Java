package br.com.senai.model.DTO.payment;

public record BuyChronosResponseDTO(Long transactionId, String qrCode, String qrCodeBase64, String expiresAt) {}
