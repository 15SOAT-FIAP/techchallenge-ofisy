package br.com.ofisy.application.serviceorderexecution;

import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionRequestDTO;
import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionResponseDTO;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;

public class ServiceOrderExecutionMapper {

    public static ServiceOrderExecution toDomain(ServiceOrderExecutionRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("ServiceOrderExecutionRequestDTO não pode ser nulo");
        }

        return ServiceOrderExecution.create(
                request.serviceCatalogId(),
                request.serviceOrderId()
        );
    }

    public static ServiceOrderExecutionResponseDTO toDTO(ServiceOrderExecution serviceOrderExecution) {
        if (serviceOrderExecution == null) {
            throw new IllegalArgumentException("ServiceOrderExecution não pode ser nulo");
        }

        return new ServiceOrderExecutionResponseDTO(
                serviceOrderExecution.getId(),
                serviceOrderExecution.getServiceCatalogId(),
                serviceOrderExecution.getServiceOrderId(),
                serviceOrderExecution.getStatus(),
                serviceOrderExecution.getCreatedAt(),
                serviceOrderExecution.getUpdatedAt(),
                serviceOrderExecution.getFinishedAt(),
                serviceOrderExecution.getStartedAt()
        );
    }
}

