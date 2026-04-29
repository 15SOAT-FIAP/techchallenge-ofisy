package br.com.ofisy.application.serviceorderexecution.dto;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceOrderExecutionResponseDTO(
        UUID id,
        UUID serviceCatalogId,
        UUID serviceOrderId,
        ServiceOrderExecutionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime finishedAt,
        LocalDateTime startedAt
) {}
