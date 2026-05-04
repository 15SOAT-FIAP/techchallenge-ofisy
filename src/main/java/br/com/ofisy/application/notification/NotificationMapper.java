package br.com.ofisy.application.notification;

import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.domain.notification.Notification;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class NotificationMapper {

    public static NotificationResponseDTO toDTO(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification não pode ser nulo");
        }

        return new NotificationResponseDTO(
                notification.getId(),
                notification.getType().name(),
                notification.getStockId(),
                notification.getQuoteId(),
                notification.getMessage(),
                notification.getRead(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }

}
