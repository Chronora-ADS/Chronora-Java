package br.com.senai.model.DTO.service;

import java.util.Map;

public record MyServiceCountsDTO(
        Map<String, Long> createdByMe,
        Map<String, Long> acceptedFromOthers
) {}
