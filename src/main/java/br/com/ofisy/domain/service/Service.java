package br.com.ofisy.domain.service;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "services")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID catalogServiceId;

    @Column(nullable = false)
    private UUID serviceExecutionTimeId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "service_status")
    private ServiceStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Service(UUID catalogServiceId, BigDecimal price) {
        this.catalogServiceId = catalogServiceId;
        this.price = price;
        this.status = ServiceStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public static Service create(UUID catalogServiceId, BigDecimal price) {
        return new Service(catalogServiceId, price);
    }

    public void setServiceExecutionTimeId(UUID serviceExecutionTimeId) {
        this.serviceExecutionTimeId = serviceExecutionTimeId;
    }

    public void complete() {
        this.status = ServiceStatus.COMPLETED;
    }

    public void cancel() {
        this.status = ServiceStatus.CANCELLED;
    }
}

