package br.com.ofisy.adapters.controllers.quote.dto;
import jakarta.validation.Valid;

import java.util.List;

public record UpdateQuoteRequestDTO(
        @Valid
        List<StockItemRequestDTO> stockItems,

        @Valid
        List<ServiceItemRequestDTO> serviceItems
) {}
