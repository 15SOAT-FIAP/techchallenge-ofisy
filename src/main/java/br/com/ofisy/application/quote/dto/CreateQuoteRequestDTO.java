package br.com.ofisy.application.quote.dto;

import jakarta.validation.Valid;

import java.util.List;

public record CreateQuoteRequestDTO(
        @Valid
        List<StockItemRequestDTO> stockItems,

        @Valid
        List<ServiceItemRequestDTO> serviceItems
) {}
