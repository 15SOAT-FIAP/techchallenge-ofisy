package br.com.ofisy.domain.serviceorderexecution;

import br.com.ofisy.domain.serviceorderexecution.exceptions.InvalidServiceOrderExecutionTransitionException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ServiceOrderExecution {

    private UUID id;
    private final UUID serviceCatalogId;
    private final UUID serviceOrderId;
    private ServiceOrderExecutionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime startedAt;

    private ServiceOrderExecution(UUID serviceCatalogId, UUID serviceOrderId) {
        this.serviceCatalogId = serviceCatalogId;
        this.serviceOrderId = serviceOrderId;
        this.status = ServiceOrderExecutionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static ServiceOrderExecution create(UUID serviceCatalogId, UUID serviceOrderId) {
        return new ServiceOrderExecution(serviceCatalogId, serviceOrderId);
    }

    public static ServiceOrderExecution reconstruct(UUID id, UUID serviceCatalogId, UUID serviceOrderId,
                                                     ServiceOrderExecutionStatus status, LocalDateTime createdAt,
                                                     LocalDateTime updatedAt, LocalDateTime startedAt, LocalDateTime finishedAt) {
        ServiceOrderExecution execution = new ServiceOrderExecution(serviceCatalogId, serviceOrderId);
        execution.id = id;
        execution.status = status;
        execution.createdAt = createdAt;
        execution.updatedAt = updatedAt;
        execution.startedAt = startedAt;
        execution.finishedAt = finishedAt;
        return execution;
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