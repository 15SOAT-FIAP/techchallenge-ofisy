package br.com.ofisy.adapters.gateways.serviceorder;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceOrderMapper {

    public static ServiceOrder toDomain(ServiceOrderEntity entity) {
        return ServiceOrder.reconstruct(
                entity.getId(),
                entity.getVehicleId(),
                entity.getCustomerId(),
                entity.getReport(),
                entity.getStatus(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getFinishedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ServiceOrderEntity toEntity(ServiceOrder serviceOrder) {
        return ServiceOrderEntity.builder()
                .id(serviceOrder.getId())
                .vehicleId(serviceOrder.getVehicleId())
                .customerId(serviceOrder.getCustomerId())
                .report(serviceOrder.getReport())
                .status(serviceOrder.getStatus())
                .createdBy(serviceOrder.getCreatedBy())
                .createdAt(serviceOrder.getCreatedAt())
                .finishedAt(serviceOrder.getFinishedAt())
                .updatedAt(serviceOrder.getUpdatedAt())
                .build();
    }
}