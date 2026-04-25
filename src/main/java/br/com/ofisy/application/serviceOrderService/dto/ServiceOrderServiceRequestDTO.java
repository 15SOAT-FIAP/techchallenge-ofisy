package br.com.ofisy.application.serviceOrderService.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ServiceOrderServiceRequestDTO(
        @NotNull(message = "ID do serviço do catálogo é obrigatório")
        UUID serviceId,
        @NotNull(message = "ID da ordem de serviço é obrigatório")
        UUID serviceOrderId
) {}
