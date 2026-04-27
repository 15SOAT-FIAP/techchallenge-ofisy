package br.com.ofisy.application.quote.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateQuoteRequestDTO(
        @NotNull(message = "ID da ordem de serviço é obrigatório")
        UUID serviceOrderId,

        @Valid
        List<StockItemRequestDTO> stockItems,

        @Valid
        List<ServiceItemRequestDTO> serviceItems
) {}
