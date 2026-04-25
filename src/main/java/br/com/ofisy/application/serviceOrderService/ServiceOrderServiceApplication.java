package br.com.ofisy.application.serviceOrderService;

import br.com.ofisy.application.serviceOrderService.dto.ServiceOrderServiceRequestDTO;
import br.com.ofisy.application.serviceOrderService.exceptions.ServiceOrderServiceNotFoundException;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderService;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderServiceRepository;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderServiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceOrderServiceApplication {

    private final ServiceOrderServiceRepository repository;

    public ServiceOrderService create(ServiceOrderServiceRequestDTO dto) {
        ServiceOrderService service = ServiceOrderService.create(
                dto.serviceId(),
                dto.serviceOrderId()
        );
        
        return repository.save(service);
    }

    public ServiceOrderService findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ServiceOrderServiceNotFoundException(id.toString()));
    }

    public Page<ServiceOrderService> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<ServiceOrderService> findByServiceId(UUID serviceId, Pageable pageable) {
        return repository.findByServiceId(serviceId, pageable);
    }

    public Page<ServiceOrderService> findByServiceOrderId(UUID serviceOrderId, Pageable pageable) {
        return repository.findByServiceOrderId(serviceOrderId, pageable);
    }

    public Page<ServiceOrderService> findByStatus(String status, Pageable pageable) {
        ServiceOrderServiceStatus serviceStatus = ServiceOrderServiceStatus.valueOf(status.toUpperCase());
        return repository.findByStatus(serviceStatus, pageable);
    }

    public ServiceOrderService complete(UUID id) {
        ServiceOrderService service = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderServiceNotFoundException(id.toString()));
        service.complete();
        return repository.save(service);
    }

    public ServiceOrderService cancel(UUID id) {
        ServiceOrderService service = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderServiceNotFoundException(id.toString()));
        service.cancel();
        return repository.save(service);
    }

    public ServiceOrderService start(UUID id) {
        ServiceOrderService service = repository.findById(id)
                .orElseThrow(() -> new ServiceOrderServiceNotFoundException(id.toString()));
        service.start();
        return repository.save(service);
    }

    public double getAverageExecutionTimeByService(UUID serviceId) {
        var serviceOrderServices = repository.findByServiceId(serviceId,
                Pageable.unpaged()).getContent();

        if (serviceOrderServices.isEmpty()) {
            return 0.0;
        }

        long totalMinutes = serviceOrderServices.stream()
                .filter(sos -> sos.getStartedAt() != null && sos.getFinishedAt() != null)
                .mapToLong(sos -> {
                    long minutes = java.time.temporal.ChronoUnit.MINUTES
                            .between(sos.getStartedAt(), sos.getFinishedAt());
                    return Math.max(minutes, 0);
                })
                .sum();

        long completedCount = serviceOrderServices.stream()
                .filter(sos -> sos.getStartedAt() != null && sos.getFinishedAt() != null)
                .count();

        return completedCount > 0 ? (double) totalMinutes / completedCount : 0.0;
    }

}
