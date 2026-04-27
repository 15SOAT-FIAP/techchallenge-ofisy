package br.com.ofisy.application.quote.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateQuoteRequestDTO(
        @NotNull(message = "ID da ordem de serviço é obrigatório")
        UUID serviceOrderId,

        @NotNull(message = "Itens de estoque são obrigatórios")
        @NotEmpty(message = "O orçamento deve ter pelo menos um item de estoque")
        @Valid
        List<StockItemRequestDTO> stockItems,

        @Valid
        List<ServiceItemRequestDTO> serviceItems
) {}
