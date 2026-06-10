package br.com.ofisy.domain.serviceorder;

import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ServiceOrder {

    private UUID id;
    private UUID vehicleId;
    private UUID customerId;
    private String report;
    private ServiceOrderStatus status;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private LocalDateTime updatedAt;

    private ServiceOrder(UUID vehicleId, UUID customerId, String report, UUID createdBy) {
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.report = report;
        this.status = ServiceOrderStatus.RECEIVED;
        this.createdBy = createdBy;
    }

    public static ServiceOrder receive(UUID vehicleId, UUID customerId, String report, UUID createdBy) {
        ServiceOrder so = new ServiceOrder(vehicleId, customerId, report, createdBy);
        so.createdAt = LocalDateTime.now();
        so.updatedAt = LocalDateTime.now();
        return so;
    }

    public static ServiceOrder reconstruct(UUID id, UUID vehicleId, UUID customerId,
            String report, ServiceOrderStatus status, UUID createdBy,
            LocalDateTime createdAt, LocalDateTime finishedAt, LocalDateTime updatedAt) {
        ServiceOrder so = new ServiceOrder(vehicleId, customerId, report, createdBy);
        so.id = id;
        so.status = status;
        so.createdAt = createdAt;
        so.finishedAt = finishedAt;
        so.updatedAt = updatedAt;
        return so;
    }

    public void startDiagnostic() {
        transitionTo(ServiceOrderStatus.IN_DIAGNOSTIC);
    }

    public void sendToApproval() {
        transitionTo(ServiceOrderStatus.AWAITING_APPROVAL);
    }

    public void approve() {
        transitionTo(ServiceOrderStatus.AWAITING_EXECUTION);
    }

    public void cancel() {
        transitionTo(ServiceOrderStatus.CANCELLED);
    }

    public void startExecution() {
        transitionTo(ServiceOrderStatus.IN_PROGRESS);
    }

    public void finish() {
        transitionTo(ServiceOrderStatus.FINISHED);
    }

    public void deliver() {
        transitionTo(ServiceOrderStatus.DELIVERED);
    }

    private void transitionTo(ServiceOrderStatus nextStatus) {
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new InvalidServiceOrderTransitionException(this.status, nextStatus);
        }
        this.status = nextStatus;
        this.updatedAt = LocalDateTime.now();
        if (nextStatus == ServiceOrderStatus.FINISHED) {
            this.finishedAt = this.updatedAt;
        }
    }
}
