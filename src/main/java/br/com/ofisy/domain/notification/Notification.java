package br.com.ofisy.domain.notification;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column
    private UUID stockId;

    @Column
    private UUID quoteId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Boolean read = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Notification(NotificationType type, UUID stockId, UUID quoteId, String message) {
        this.type = type;
        this.stockId = stockId;
        this.quoteId = quoteId;
        this.message = message;
        this.read = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Notification createForStock(UUID stockId, String message) {
        return new Notification(NotificationType.LOW_STOCK, stockId, null, message);
    }

    public static Notification createForQuote(UUID quoteId, String message) {
        return new Notification(NotificationType.QUOTE_GENERATED, null, quoteId, message);
    }

    public void markAsRead() {
        this.read = true;
        this.updatedAt = LocalDateTime.now();
    }
}
