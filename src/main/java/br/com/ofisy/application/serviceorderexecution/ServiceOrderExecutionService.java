package br.com.ofisy.application.serviceorderexecution;

import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionRequestDTO;
import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionResponseDTO;
import br.com.ofisy.application.serviceorderexecution.exceptions.ServiceOrderExecutionNotFoundException;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
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

    public ServiceOrderExecutionResponseDTO create(ServiceOrderExecutionRequestDTO dto) {
        ServiceOrderExecution service = ServiceOrderExecution.create(
                dto.serviceCatalogId(),
                dto.serviceOrderId()
        );
        
        ServiceOrderExecution savedService = repository.save(service);
        return ServiceOrderExecutionMapper.toDTO(savedService);
    }

    public ServiceOrderExecutionResponseDTO findById(UUID id) {
        return repository.findById(id)
                .map(ServiceOrderExecutionMapper::toDTO)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id.toString()));
    }

    public Page<ServiceOrderExecutionResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(ServiceOrderExecutionMapper::toDTO);
    }

    public Page<ServiceOrderExecutionResponseDTO> findByServiceCatalogId(UUID serviceCatalogId, Pageable pageable) {
        return repository.findByServiceCatalogId(serviceCatalogId, pageable)
                .map(ServiceOrderExecutionMapper::toDTO);
    }

    public Page<ServiceOrderExecutionResponseDTO> findByServiceOrderId(UUID serviceOrderId, Pageable pageable) {
        return repository.findByServiceOrderId(serviceOrderId, pageable)
                .map(ServiceOrderExecutionMapper::toDTO);
    }

    public Page<ServiceOrderExecutionResponseDTO> findByStatus(String status, Pageable pageable) {
        ServiceOrderExecutionStatus serviceStatus = ServiceOrderExecutionStatus.valueOf(status.toUpperCase());
        return repository.findByStatus(serviceStatus, pageable)
                .map(ServiceOrderExecutionMapper::toDTO);
    }

    public ServiceOrderExecutionResponseDTO complete(UUID id) {
        ServiceOrderExecution service = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id.toString()));
        service.complete();
        return ServiceOrderExecutionMapper.toDTO(repository.save(service));
    }

    public ServiceOrderExecutionResponseDTO cancel(UUID id) {
        ServiceOrderExecution service = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id.toString()));
        service.cancel();
        return ServiceOrderExecutionMapper.toDTO(repository.save(service));
    }

    public ServiceOrderExecutionResponseDTO start(UUID id) {
        ServiceOrderExecution service = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id.toString()));
        service.start();
        return ServiceOrderExecutionMapper.toDTO(repository.save(service));
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
