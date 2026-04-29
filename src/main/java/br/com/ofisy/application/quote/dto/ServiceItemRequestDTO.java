package br.com.ofisy.application.quote.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ServiceItemRequestDTO(
        @NotNull(message = "ID da execução do serviço é obrigatório")
        UUID serviceOrderExecutionId
) {}
