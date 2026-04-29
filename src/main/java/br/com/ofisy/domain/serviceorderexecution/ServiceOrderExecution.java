package br.com.ofisy.domain.serviceorderexecution;

import br.com.ofisy.domain.serviceorderexecution.exceptions.InvalidServiceOrderExecutionTransitionException;
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

    public void start() {
        transitionTo(ServiceOrderExecutionStatus.IN_PROGRESS);
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        transitionTo(ServiceOrderExecutionStatus.COMPLETED);
        this.finishedAt = LocalDateTime.now();
    }

    public void cancel() {
        transitionTo(ServiceOrderExecutionStatus.CANCELLED);
        this.finishedAt = LocalDateTime.now();
    }

    private void transitionTo(ServiceOrderExecutionStatus next) {
        if (!this.status.canTransitionTo(next)) {
            throw new InvalidServiceOrderExecutionTransitionException(this.status, next);
        }
        this.status = next;
        this.updatedAt = LocalDateTime.now();
    }
}