package br.com.ofisy.application.notification.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record QuoteNotificationRequestDTO(
        UUID id,
        UUID serviceOrderId,
        BigDecimal totalPrice
) {}
