package br.com.ofisy.domain.notification;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Notification {
    private final UUID id;
    private final NotificationType type;
    private final UUID stockId;
    private final UUID quoteId;
    private final NotificationMessage message;
    private boolean read;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Notification(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.stockId = builder.stockId;
        this.quoteId = builder.quoteId;
        this.message = builder.message;
        this.read = builder.read;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Notification createForStock(UUID stockId, NotificationMessage message) {
        return builder()
            .id(UUID.randomUUID())
            .type(NotificationType.LOW_STOCK)
            .stockId(stockId)
            .message(message)
            .read(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public static Notification createForQuote(UUID quoteId, NotificationMessage message) {
        return builder()
            .id(UUID.randomUUID())
            .type(NotificationType.QUOTE_GENERATED)
            .quoteId(quoteId)
            .message(message)
            .read(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private NotificationType type;
        private UUID stockId;
        private UUID quoteId;
        private NotificationMessage message;
        private boolean read;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder type(NotificationType type) { this.type = type; return this; }
        public Builder stockId(UUID stockId) { this.stockId = stockId; return this; }
        public Builder quoteId(UUID quoteId) { this.quoteId = quoteId; return this; }
        public Builder message(NotificationMessage message) { this.message = message; return this; }
        public Builder read(boolean read) { this.read = read; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Notification build() {
            return new Notification(this);
        }
    }

    public void markAsRead() {
        this.read = true;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public NotificationType getType() {
        return type;
    }

    public UUID getStockId() {
        return stockId;
    }

    public UUID getQuoteId() {
        return quoteId;
    }

    public NotificationMessage getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return read == that.read && 
               Objects.equals(id, that.id) && 
               type == that.type && 
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
        return "Notification{" +
               "id=" + id +
               ", type=" + type +
               ", stockId=" + stockId +
               ", quoteId=" + quoteId +
               ", message=" + message +
               ", read=" + read +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}
