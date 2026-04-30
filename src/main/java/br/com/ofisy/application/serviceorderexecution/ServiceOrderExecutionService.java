package br.com.ofisy.application.serviceorderexecution;

import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionRequestDTO;
import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionResponseDTO;
import br.com.ofisy.application.serviceorderexecution.exceptions.ServiceOrderExecutionNotFoundException;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceOrderExecutionService {

    private final ServiceOrderExecutionRepository repository;

    @Transactional
    public ServiceOrderExecutionResponseDTO create(ServiceOrderExecutionRequestDTO dto) {
        ServiceOrderExecution service = ServiceOrderExecutionMapper.toDomain(dto);
        ServiceOrderExecution savedService = repository.save(service);
        return ServiceOrderExecutionMapper.toDTO(savedService);
    }

    @Transactional(readOnly = true)
    public ServiceOrderExecutionResponseDTO findById(UUID id) {
        return repository.findById(id)
                .map(ServiceOrderExecutionMapper::toDTO)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<ServiceOrderExecutionResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(ServiceOrderExecutionMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ServiceOrderExecutionResponseDTO> findByServiceCatalogId(UUID serviceCatalogId, Pageable pageable) {
        return repository.findByServiceCatalogId(serviceCatalogId, pageable)
                .map(ServiceOrderExecutionMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ServiceOrderExecutionResponseDTO> findByServiceOrderId(UUID serviceOrderId, Pageable pageable) {
        return repository.findByServiceOrderId(serviceOrderId, pageable)
                .map(ServiceOrderExecutionMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ServiceOrderExecutionResponseDTO> findByStatus(String status, Pageable pageable) {
        ServiceOrderExecutionStatus serviceStatus = ServiceOrderExecutionStatus.valueOf(status.toUpperCase());
        return repository.findByStatus(serviceStatus, pageable)
                .map(ServiceOrderExecutionMapper::toDTO);
    }

    @Transactional
    public ServiceOrderExecutionResponseDTO complete(UUID id) {
        ServiceOrderExecution service = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id));
        service.complete();
        return ServiceOrderExecutionMapper.toDTO(repository.save(service));
    }

    @Transactional
    public ServiceOrderExecutionResponseDTO cancel(UUID id) {
        ServiceOrderExecution service = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id));
        service.cancel();
        return ServiceOrderExecutionMapper.toDTO(repository.save(service));
    }

    @Transactional
    public ServiceOrderExecutionResponseDTO start(UUID id) {
        ServiceOrderExecution service = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderExecutionNotFoundException(id));
        service.start();
        return ServiceOrderExecutionMapper.toDTO(repository.save(service));
    }

    @Transactional(readOnly = true)
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
