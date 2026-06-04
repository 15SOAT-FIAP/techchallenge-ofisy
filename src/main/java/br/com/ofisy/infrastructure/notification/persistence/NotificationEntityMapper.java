package br.com.ofisy.infrastructure.notification.persistence;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import org.springframework.stereotype.Component;

@Component
public class NotificationEntityMapper {

    public NotificationJpaEntity toEntity(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(notification.getId());
        entity.setType(notification.getType());
        entity.setStockId(notification.getStockId());
        entity.setQuoteId(notification.getQuoteId());
        entity.setMessage(notification.getMessage().getContent());
        entity.setRead(notification.isRead());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setUpdatedAt(notification.getUpdatedAt());
        return entity;
    }

    public Notification toDomain(NotificationJpaEntity entity) {
        NotificationMessage message = NotificationMessage.fromString(entity.getMessage());

        return Notification.builder()
            .id(entity.getId())
            .type(entity.getType())
            .stockId(entity.getStockId())
            .quoteId(entity.getQuoteId())
            .message(message)
            .read(entity.getRead())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
