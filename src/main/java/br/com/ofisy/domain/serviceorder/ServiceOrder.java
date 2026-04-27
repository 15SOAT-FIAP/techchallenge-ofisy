package br.com.ofisy.domain.serviceorder;

import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID vehicleId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(columnDefinition = "TEXT")
    private String report;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceOrderStatus status;

    @Column(nullable = false)
    private UUID createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

//    private List<ServiceOrderService> services = new ArrayList<>();
//    private List<Quote> quotes = new ArrayList<>();

    private ServiceOrder(UUID vehicleId, UUID customerId, String report, UUID createdBy) {
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.report = report;
        this.status = ServiceOrderStatus.RECEIVED;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static ServiceOrder receive(UUID vehicleId, UUID customerId, String report, UUID createdBy) {
        return new ServiceOrder(vehicleId, customerId, report, createdBy);
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
