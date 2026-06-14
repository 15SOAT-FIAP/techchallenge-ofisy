package br.com.ofisy.adapters.controllers.stock.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record UpdateStockRequestDTO(

        String productName,

        String description,

        @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que zero")
        BigDecimal unitPrice,

        String category,

        @Min(value = 0, message = "Limite mínimo não pode ser negativo")
        Integer minThreshold
) {}
