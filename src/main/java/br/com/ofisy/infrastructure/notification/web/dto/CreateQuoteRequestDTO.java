package br.com.ofisy.infrastructure.notification.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateQuoteRequestDTO(
    UUID quoteId,
    UUID serviceOrderId,
    BigDecimal totalPrice
) {}
