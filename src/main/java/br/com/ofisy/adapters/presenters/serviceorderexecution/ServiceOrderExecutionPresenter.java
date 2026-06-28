package br.com.ofisy.adapters.presenters.serviceorderexecution;

import br.com.ofisy.adapters.controllers.serviceorderexecution.dto.ServiceOrderExecutionResponseDTO;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceOrderExecutionPresenter {

    public static ServiceOrderExecutionResponseDTO present(ServiceOrderExecution serviceOrderExecution) {
        return new ServiceOrderExecutionResponseDTO(
                serviceOrderExecution.getId(),
                serviceOrderExecution.getServiceCatalogId(),
                serviceOrderExecution.getServiceOrderId(),
                serviceOrderExecution.getStatus(),
                serviceOrderExecution.getCreatedAt(),
                serviceOrderExecution.getUpdatedAt(),
                serviceOrderExecution.getStartedAt(),
                serviceOrderExecution.getFinishedAt()
        );
    }
}
