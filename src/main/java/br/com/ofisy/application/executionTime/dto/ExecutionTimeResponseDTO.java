package br.com.ofisy.application.executionTime.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExecutionTimeResponseDTO(
        UUID id,
        UUID serviceId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        long durationInMinutes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

