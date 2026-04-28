package br.com.ofisy.application.serviceOrderExecution.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ServiceOrderExecutionRequestDTO(
        @NotNull(message = "ID do serviço do catálogo é obrigatório")
        UUID serviceCatalogId,
        @NotNull(message = "ID da ordem de serviço é obrigatório")
        UUID serviceOrderId
) {}
