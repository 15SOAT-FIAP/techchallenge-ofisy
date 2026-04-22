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

    @Column(name = "stock_id")
    private UUID stockId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean read;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public static Notification createStockAlert(UUID stockId, String message) {
        return new Notification(stockId, message);
    }

    public void markAsRead() {
        this.read = true;
        this.updatedAt = LocalDateTime.now();
    }

    private Notification(UUID stockId,

                         String message) {
        this.stockId = stockId;
        this.message = message;
        this.read = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}