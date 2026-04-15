package br.com.ofisy.application.notification;

import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.domain.notification.Notification;

public class NotificationMapper {

    private NotificationMapper() {
        /* utility class */
    }

    public static NotificationResponseDTO toDTO(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification não pode ser nulo");
        }
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getStockId(),
//                notification.getServiceOrderId(),
//                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}