package br.com.ofisy.application.notification;

import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.domain.notification.Notification;

public class NotificationMapper {

    private NotificationMapper() {
        /* This utility class should not be instantiated */
    }

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
