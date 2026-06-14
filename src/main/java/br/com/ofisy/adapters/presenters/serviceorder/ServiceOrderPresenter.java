package br.com.ofisy.adapters.presenters.serviceorder;

import br.com.ofisy.adapters.controllers.serviceorder.dto.ServiceOrderResponseDTO;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceOrderPresenter {

    public static ServiceOrderResponseDTO present(ServiceOrder serviceOrder) {
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
}