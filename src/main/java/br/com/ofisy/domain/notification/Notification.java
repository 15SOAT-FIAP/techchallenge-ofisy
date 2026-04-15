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

//    @Column(name = "service_order_id")
//    private UUID serviceOrderId;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private NotificationType type;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean read;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    private Notification(UUID stockId,
//                         UUID serviceOrderId,
//                         NotificationType type,
                         String message) {
        this.stockId = stockId;
//        this.serviceOrderId = serviceOrderId;
//        this.type = type;
        this.message = message;
        this.read = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

//    public static Notification createStockAlert(UUID stockId, String message) {
//        return new Notification(stockId, null, NotificationType.STOCK, message);
//    }
//
//    public static Notification createServiceOrderAlert(UUID serviceOrderId, String message) {
//        return new Notification(null, serviceOrderId, NotificationType.SERVICE_ORDER, message);
//    }
}