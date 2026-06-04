package br.com.ofisy.infrastructure.notification.web;

import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.application.notification.ports.in.CreateLowStockCommand;
import br.com.ofisy.application.notification.ports.in.CreateQuoteCommand;
import br.com.ofisy.application.notification.ports.in.NotificationResponse;
import br.com.ofisy.infrastructure.notification.web.dto.CreateLowStockRequestDTO;
import br.com.ofisy.infrastructure.notification.web.dto.CreateQuoteRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationDTOMapperTest {

    @Test
    @DisplayName("Deve converter CreateLowStockRequestDTO para Command")
    void shouldConvertCreateLowStockRequestToCommand() {
        UUID stockId = UUID.randomUUID();
        CreateLowStockRequestDTO dto = new CreateLowStockRequestDTO(
            stockId,
            "Radiador",
            2,
            5
        );

        CreateLowStockCommand command = NotificationDTOMapper.toCommand(dto);

        assertThat(command.stockId()).isEqualTo(stockId);
        assertThat(command.productName()).isEqualTo("Radiador");
        assertThat(command.currentQuantity()).isEqualTo(2);
        assertThat(command.minThreshold()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve converter CreateQuoteRequestDTO para Command")
    void shouldConvertCreateQuoteRequestToCommand() {
        UUID quoteId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        BigDecimal totalPrice = new BigDecimal("1500.00");

        CreateQuoteRequestDTO dto = new CreateQuoteRequestDTO(
            quoteId,
            serviceOrderId,
            totalPrice
        );

        CreateQuoteCommand command = NotificationDTOMapper.toCommand(dto);

        assertThat(command.quoteId()).isEqualTo(quoteId);
        assertThat(command.serviceOrderId()).isEqualTo(serviceOrderId);
        assertThat(command.totalPrice()).isEqualTo(totalPrice);
    }

    @Test
    @DisplayName("Deve converter NotificationResponse para DTO")
    void shouldConvertNotificationResponseToDTO() {
        UUID id = UUID.randomUUID();
        UUID stockId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        NotificationResponse response = new NotificationResponse(
            id,
            "LOW_STOCK",
            stockId,
            null,
            "Estoque baixo para Radiador",
            false,
            now,
            now
        );

        NotificationResponseDTO dto = NotificationDTOMapper.toDTO(response);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.type()).isEqualTo("LOW_STOCK");
        assertThat(dto.stockId()).isEqualTo(stockId);
        assertThat(dto.quoteId()).isNull();
        assertThat(dto.message()).isEqualTo("Estoque baixo para Radiador");
        assertThat(dto.read()).isFalse();
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.updatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Deve preservar todos os campos na conversão")
    void shouldPreserveAllFieldsInConversion() {
        UUID id = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        NotificationResponse response = new NotificationResponse(
            id,
            "QUOTE_GENERATED",
            null,
            quoteId,
            "Orçamento gerado",
            true,
            createdAt,
            updatedAt
        );

        NotificationResponseDTO dto = NotificationDTOMapper.toDTO(response);

        assertThat(dto.read()).isTrue();
        assertThat(dto.createdAt()).isEqualTo(createdAt);
        assertThat(dto.updatedAt()).isEqualTo(updatedAt);
    }
}
