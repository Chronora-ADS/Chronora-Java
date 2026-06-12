package br.com.senai.model.DTO.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SellChronosRequestDTO(@NotNull @Min(1) Integer chronosAmount, @NotBlank String pixKey) {}
