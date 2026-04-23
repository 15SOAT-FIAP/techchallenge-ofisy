package br.com.ofisy.domain.serviceOrderService;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_order_services")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceOrderService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID serviceId;

    // id da ordem de serviço a qual este serviço pertence
    @Column(nullable = false)
    private UUID serviceOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "service_status")
    private ServiceOrderServiceStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime finishedAt;

    @Column
    private LocalDateTime startedAt;



    private ServiceOrderService(UUID serviceId, UUID serviceOrderId) {
        this.serviceId = serviceId;
        this.status = ServiceOrderServiceStatus.PENDING;
        this.serviceOrderId = serviceOrderId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static ServiceOrderService create(UUID serviceId, UUID serviceOrderId) {
        return new ServiceOrderService(serviceId, serviceOrderId);
    }

    public void complete() {
        this.status = ServiceOrderServiceStatus.COMPLETED;
        this.finishedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = ServiceOrderServiceStatus.CANCELLED;
        this.finishedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void start() {
        this.status = ServiceOrderServiceStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


}
