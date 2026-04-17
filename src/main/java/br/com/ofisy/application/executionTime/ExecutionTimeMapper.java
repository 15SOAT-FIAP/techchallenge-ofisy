package br.com.ofisy.application.executionTime;

import br.com.ofisy.application.service.dto.ExecutionTimeResponseDTO;
import br.com.ofisy.domain.executionTime.ServiceExecutionTime;
import org.springframework.stereotype.Component;

@Component
public class ExecutionTimeMapper {

    public ExecutionTimeResponseDTO toResponse(ServiceExecutionTime executionTime) {
        return new ExecutionTimeResponseDTO(
                executionTime.getId(),
                executionTime.getServiceId(),
                executionTime.getStartDate(),
                executionTime.getEndDate(),
                executionTime.getDurationInMinutes(),
                executionTime.getCreatedAt(),
                executionTime.getUpdatedAt()
        );
    }
}

