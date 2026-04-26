package br.com.ofisy.application.notification;

import br.com.ofisy.application.notification.dto.CreateNotificationRequestDTO;
import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.domain.notification.Notification;

public class NotificationMapper {

    public static NotificationResponseDTO toDTO(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification não pode ser nulo");
        }

        return new NotificationResponseDTO(
                notification.getId(),
                notification.getType(),
                notification.getStockId(),
                notification.getMessage(),
                notification.getRead(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }

    public static Notification toDomain(CreateNotificationRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CreateNotificationRequestDTO não pode ser nulo");
        }

        return Notification.create(
                dto.type(),
                dto.stockId(),
                dto.message()
        );
    }
}
