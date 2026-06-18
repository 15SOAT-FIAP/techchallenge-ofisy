package br.com.ofisy.application.serviceorderexecution.getaverageexecutiontime;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetAverageExecutionTimeService implements GetAverageExecutionTimeServiceOrderExecutionUseCase {

    private final ServiceOrderExecutionRepository repository;

    public GetAverageExecutionTimeService(ServiceOrderExecutionRepository repository) {
        this.repository = repository;
    }

    @Override
    public double execute(UUID serviceCatalogId) {
        if (serviceCatalogId == null) {
            throw new IllegalArgumentException("Service Catalog ID não pode ser nulo");
        }
        
        List<ServiceOrderExecution> serviceOrderExecutions = repository.findByServiceCatalogId(serviceCatalogId,
                Pageable.unpaged()).getContent();

        if (serviceOrderExecutions.isEmpty()) {
            return 0.0;
        }

        long totalMinutes = serviceOrderExecutions.stream()
                .filter(sos -> sos.getStartedAt() != null && sos.getFinishedAt() != null)
                .mapToLong(sos -> {
                    long minutes = java.time.temporal.ChronoUnit.MINUTES
                            .between(sos.getStartedAt(), sos.getFinishedAt());
                    return Math.max(minutes, 0);
                })
                .sum();

        long completedCount = serviceOrderExecutions.stream()
                .filter(sos -> sos.getStartedAt() != null && sos.getFinishedAt() != null)
                .count();

        return completedCount > 0 ? (double) totalMinutes / completedCount : 0.0;
    }
}

