package br.com.ofisy.application.quote.dto;

import jakarta.validation.constraints.NotNull;

public record ReproveQuoteRequestDTO(
        @NotNull(message = "Motivo da reprovação é obrigatório")
        String reason
) {}
