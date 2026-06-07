package br.com.ofisy.adapters.gateways.notification;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationMapper {

    public static Notification toDomain(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }
        return Notification.builder()
                .id(entity.getId())
                .type(entity.getType())
                .stockId(entity.getStockId())
                .quoteId(entity.getQuoteId())
                .message(NotificationMessage.fromString(entity.getMessage()))
                .read(entity.getRead())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static NotificationEntity toEntity(Notification domain) {
        if (domain == null) {
            return null;
        }
        return NotificationEntity.builder()
                .id(domain.getId())
                .type(domain.getType())
                .stockId(domain.getStockId())
                .quoteId(domain.getQuoteId())
                .message(domain.getMessage().getContent())
                .read(domain.isRead())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
