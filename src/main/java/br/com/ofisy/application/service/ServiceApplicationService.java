package br.com.ofisy.application.service;

import br.com.ofisy.application.executionTime.ExecutionTimeService;
import br.com.ofisy.application.service.dto.ServiceRequestDTO;
import br.com.ofisy.application.service.exceptions.ServiceNotFoundException;
import br.com.ofisy.domain.service.Service;
import br.com.ofisy.domain.service.ServiceRepository;
import br.com.ofisy.domain.service.ServiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceApplicationService {

    private final ServiceRepository repository;
    private final ServiceMapper mapper;
    private final ExecutionTimeService executionTimeService;

    public Service create(ServiceRequestDTO request) {
        var service = mapper.toDomain(request);
        
        var saved = repository.save(service);
        
        var executionTime = executionTimeService.create(saved.getId());

        saved.setServiceExecutionTimeId(executionTime.getId());
        return repository.save(saved);
    }

    public Service findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id.toString()));
    }

    public Page<Service> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Service> findByCustomerId(UUID customerId, Pageable pageable) {
        return repository.findByCustomerId(customerId, pageable);
    }

    public Page<Service> findByCatalogServiceId(UUID catalogServiceId, Pageable pageable) {
        return repository.findByCatalogServiceId(catalogServiceId, pageable);
    }

    public Page<Service> findByStatus(String status, Pageable pageable) {
        var serviceStatus = ServiceStatus.valueOf(status.toUpperCase());
        return repository.findByStatus(serviceStatus, pageable);
    }

    public Service completeService(UUID id) {
        var service = repository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id.toString()));
        service.complete();
        return repository.save(service);
    }

    public Service cancelService(UUID id) {
        var service = repository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id.toString()));
        service.cancel();
        return repository.save(service);
    }
}
