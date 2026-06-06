package br.com.ofisy.adapters.controllers.notification.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateQuoteRequestDTO(
        UUID id,
        UUID serviceOrderId,
        BigDecimal totalPrice
) {}
