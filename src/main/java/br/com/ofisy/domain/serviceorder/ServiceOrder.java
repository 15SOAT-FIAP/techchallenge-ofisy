package br.com.ofisy.domain.serviceorder;

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
import java.util.ArrayList;
import java.util.List;
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

//    private List<ServiceOrderExecution> services = new ArrayList<>();
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
}
