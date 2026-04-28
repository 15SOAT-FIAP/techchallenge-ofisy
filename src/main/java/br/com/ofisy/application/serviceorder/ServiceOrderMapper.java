package br.com.ofisy.application.serviceorder;

import br.com.ofisy.application.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderResponseDTO;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderStatusResponseDTO;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceOrderMapper {

    public static ServiceOrderResponseDTO toResponseDTO(ServiceOrder serviceOrder) {
        if (serviceOrder == null) {
            throw new IllegalArgumentException("ServiceOrder não pode ser nulo");
        }
        return new ServiceOrderResponseDTO(
                serviceOrder.getId(),
                serviceOrder.getVehicleId(),
                serviceOrder.getCustomerId(),
                serviceOrder.getReport(),
                serviceOrder.getStatus().name(),
                serviceOrder.getCreatedBy(),
                serviceOrder.getCreatedAt(),
                serviceOrder.getFinishedAt(),
                serviceOrder.getUpdatedAt()
        );
    }

    public static ServiceOrder toDomain(ServiceOrderRequestDTO request, UUID createdBy) {
        if (request == null) {
            throw new IllegalArgumentException("ServiceOrderRequestDTO não pode ser nulo");
        }
        return ServiceOrder.receive(
                request.vehicleId(),
                request.customerId(),
                request.report(),
                createdBy
        );
    }

    public static ServiceOrderStatusResponseDTO toStatusResponseDTO(ServiceOrder serviceOrder) {
        if (serviceOrder == null) {
            throw new IllegalArgumentException("ServiceOrder não pode ser nulo");
        }
        return new ServiceOrderStatusResponseDTO(
                serviceOrder.getId(),
                serviceOrder.getVehicleId(),
                serviceOrder.getCustomerId(),
                serviceOrder.getStatus()
        );
    }
}
