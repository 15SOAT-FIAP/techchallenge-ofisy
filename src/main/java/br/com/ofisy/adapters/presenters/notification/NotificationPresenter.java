package br.com.ofisy.adapters.presenters.notification;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.adapters.controllers.notification.dto.NotificationResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationPresenter {

    public static NotificationResponseDTO present(Notification notification) {
        if (notification == null) {
            return null;
        }
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getType().name(),
                notification.getStockId(),
                notification.getQuoteId(),
                notification.getMessage().getContent(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}
