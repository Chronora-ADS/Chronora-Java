package br.com.senai.model.DTO.service;

import br.com.senai.model.entity.ServiceEntity;
import br.com.senai.model.enums.ServiceModality;
import br.com.senai.model.enums.ServiceStatus;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public record ModeratorServiceSummaryDTO(
        Long id,
        String title,
        ServiceStatus status,
        ServiceModality modality,
        Integer timeChronos,
        LocalDate deadline,
        String postedAt,
        String creatorName,
        Long creatorId,
        String acceptedName,
        Long acceptedId,
        List<String> categories
) {
    public static ModeratorServiceSummaryDTO from(ServiceEntity s) {
        return new ModeratorServiceSummaryDTO(
                s.getId(),
                s.getTitle(),
                s.getStatus(),
                s.getModality(),
                s.getTimeChronos(),
                s.getDeadline(),
                s.getPostedAt().atOffset(ZoneOffset.UTC).toString(),
                s.getUserCreator() != null ? s.getUserCreator().getName() : "Desconhecido",
                s.getUserCreator() != null ? s.getUserCreator().getId() : null,
                s.getUserAccepted() != null ? s.getUserAccepted().getName() : null,
                s.getUserAccepted() != null ? s.getUserAccepted().getId() : null,
                s.getCategories()
        );
    }
}
