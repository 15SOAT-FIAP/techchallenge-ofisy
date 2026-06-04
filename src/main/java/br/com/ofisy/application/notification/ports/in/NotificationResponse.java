package br.com.ofisy.application.notification.ports.in;

import br.com.ofisy.domain.notification.Notification;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    String type,
    UUID stockId,
    UUID quoteId,
    String message,
    boolean read,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationResponse that = (NotificationResponse) o;
        return read == that.read && 
               Objects.equals(id, that.id) && 
               Objects.equals(type, that.type) && 
               Objects.equals(stockId, that.stockId) && 
               Objects.equals(quoteId, that.quoteId) && 
               Objects.equals(message, that.message) && 
               Objects.equals(createdAt, that.createdAt) && 
               Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, stockId, quoteId, message, read, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "NotificationResponse{" +
               "id=" + id +
               ", type=" + type +
               ", stockId=" + stockId +
               ", quoteId=" + quoteId +
               ", message='" + message + '\'' +
               ", read=" + read +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}
