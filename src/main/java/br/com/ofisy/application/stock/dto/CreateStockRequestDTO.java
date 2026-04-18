package br.com.ofisy.application.stock.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CreateStockRequestDTO(

        @NotBlank(message = "Nome do produto é obrigatório")
        String productName,

        @NotBlank(message = "Descrição é obrigatório")
        String description,

        @NotBlank(message = "Quantidade é obrigatório")
        Integer quantity,

        @NotBlank(message = "Preço unitário é obrigatório")
        BigDecimal unitPrice,

        @NotBlank(message = "Categoria é obrigatório")
        String category,

        @NotBlank(message = "Limite mínimo é obrigatório")
        Integer minThreshold
) {}
