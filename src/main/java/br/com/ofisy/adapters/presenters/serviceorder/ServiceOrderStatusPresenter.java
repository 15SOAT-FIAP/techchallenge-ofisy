package br.com.ofisy.adapters.presenters.serviceorder;

import br.com.ofisy.adapters.controllers.serviceorder.dto.ServiceOrderStatusResponseDTO;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceOrderStatusPresenter {

    public static ServiceOrderStatusResponseDTO present(ServiceOrder serviceOrder) {
        return new ServiceOrderStatusResponseDTO(
                serviceOrder.getId(),
                serviceOrder.getVehicleId(),
                serviceOrder.getCustomerId(),
                serviceOrder.getStatus(),
                serviceOrder.getUpdatedAt()
        );
    }
}