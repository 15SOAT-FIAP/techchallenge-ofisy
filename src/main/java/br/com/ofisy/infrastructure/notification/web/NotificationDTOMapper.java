package br.com.ofisy.infrastructure.notification.web;

import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.application.notification.ports.in.CreateLowStockCommand;
import br.com.ofisy.application.notification.ports.in.CreateQuoteCommand;
import br.com.ofisy.application.notification.ports.in.NotificationResponse;
import br.com.ofisy.infrastructure.notification.web.dto.CreateLowStockRequestDTO;
import br.com.ofisy.infrastructure.notification.web.dto.CreateQuoteRequestDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationDTOMapper {

    public static CreateLowStockCommand toCommand(CreateLowStockRequestDTO dto) {
        return new CreateLowStockCommand(
            dto.stockId(),
            dto.productName(),
            dto.currentQuantity(),
            dto.minThreshold()
        );
    }

    public static CreateQuoteCommand toCommand(CreateQuoteRequestDTO dto) {
        return new CreateQuoteCommand(
            dto.quoteId(),
            dto.serviceOrderId(),
            dto.totalPrice()
        );
    }

    public static NotificationResponseDTO toDTO(NotificationResponse response) {
        return new NotificationResponseDTO(
            response.id(),
            response.type(),
            response.stockId(),
            response.quoteId(),
            response.message(),
            response.read(),
            response.createdAt(),
            response.updatedAt()
        );
    }
}
