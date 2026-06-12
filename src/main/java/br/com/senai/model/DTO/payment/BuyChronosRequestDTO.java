package br.com.senai.model.DTO.payment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BuyChronosRequestDTO(@NotNull @Min(1) @Max(300) Integer chronosAmount) {}
