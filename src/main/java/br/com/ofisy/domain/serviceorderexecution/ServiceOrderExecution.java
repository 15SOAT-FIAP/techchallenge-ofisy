package br.com.ofisy.domain.serviceorderexecution;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_order_executions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceOrderExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID serviceCatalogId;

    @Column(nullable = false)
    private UUID serviceOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceOrderExecutionStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime finishedAt;

    @Column
    private LocalDateTime startedAt;

    private ServiceOrderExecution(UUID serviceCatalogId, UUID serviceOrderId) {
        this.serviceCatalogId = serviceCatalogId;
        this.status = ServiceOrderExecutionStatus.PENDING;
        this.serviceOrderId = serviceOrderId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static ServiceOrderExecution create(UUID serviceCatalogId, UUID serviceOrderId) {
        return new ServiceOrderExecution(serviceCatalogId, serviceOrderId);
    }

    public void complete() {
        this.status = ServiceOrderExecutionStatus.COMPLETED;
        this.finishedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = ServiceOrderExecutionStatus.CANCELLED;
        this.finishedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void start() {
        this.status = ServiceOrderExecutionStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
