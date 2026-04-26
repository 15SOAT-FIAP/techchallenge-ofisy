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

    @Column(nullable = false, length = 100)
    private String type;

    @Column
    private UUID stockId;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private Boolean read = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Notification(String type, UUID stockId, String message) {
        this.type = type;
        this.stockId = stockId;
        this.message = message;
        this.read = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Notification create(String type, UUID stockId, String message) {
        return new Notification(type, stockId, message);
    }

    public void markAsRead() {
        this.read = true;
        this.updatedAt = LocalDateTime.now();
    }
}
