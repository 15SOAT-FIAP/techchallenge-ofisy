package br.com.ofisy.application.serviceOrderExecution;

import br.com.ofisy.application.serviceOrderExecution.dto.ServiceOrderExecutionRequestDTO;
import br.com.ofisy.application.serviceOrderExecution.exceptions.ServiceOrderExecutionNotFoundException;
import br.com.ofisy.domain.serviceOrderExecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceOrderExecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceOrderExecution.ServiceOrderExecutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceOrderExecutionService {

    private final ServiceOrderExecutionRepository repository;

    public ServiceOrderExecution create(ServiceOrderExecutionRequestDTO dto) {
        ServiceOrderExecution service = ServiceOrderExecution.create(
                dto.serviceCatalogId(),
                dto.serviceOrderId()
        );
        
        return repository.save(service);
    }

    public ServiceOrderExecution findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id.toString()));
    }

    public Page<ServiceOrderExecution> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<ServiceOrderExecution> findByServiceCatalogId(UUID serviceCatalogId, Pageable pageable) {
        return repository.findByServiceCatalogId(serviceCatalogId, pageable);
    }

    public Page<ServiceOrderExecution> findByServiceOrderId(UUID serviceOrderId, Pageable pageable) {
        return repository.findByServiceOrderId(serviceOrderId, pageable);
    }

    public Page<ServiceOrderExecution> findByStatus(String status, Pageable pageable) {
        ServiceOrderExecutionStatus serviceStatus = ServiceOrderExecutionStatus.valueOf(status.toUpperCase());
        return repository.findByStatus(serviceStatus, pageable);
    }

    public ServiceOrderExecution complete(UUID id) {
        ServiceOrderExecution service = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id.toString()));
        service.complete();
        return repository.save(service);
    }

    public ServiceOrderExecution cancel(UUID id) {
        ServiceOrderExecution service = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id.toString()));
        service.cancel();
        return repository.save(service);
    }

    public ServiceOrderExecution start(UUID id) {
        ServiceOrderExecution service = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id.toString()));
        service.start();
        return repository.save(service);
    }

    public double getAverageExecutionTimeByService(UUID serviceCatalogId) {
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
