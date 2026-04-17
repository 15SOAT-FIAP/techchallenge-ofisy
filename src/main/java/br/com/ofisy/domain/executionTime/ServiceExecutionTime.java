package br.com.ofisy.domain.executionTime;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_execution_times")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceExecutionTime {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID serviceId;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private ServiceExecutionTime(UUID serviceId) {
        this.serviceId = serviceId;
        this.startDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static ServiceExecutionTime create(UUID serviceId) {
        return new ServiceExecutionTime(serviceId);
    }

    public void finish() {
        this.endDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public long getDurationInMinutes() {
        if (endDate == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.MINUTES.between(startDate, endDate);
    }
}

