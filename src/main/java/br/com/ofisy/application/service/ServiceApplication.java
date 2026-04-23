package br.com.ofisy.application.service;

import br.com.ofisy.application.service.dto.ServiceRequestDTO;
import br.com.ofisy.application.serviceOrderService.dto.ServiceOrderServiceRequestDTO;
import br.com.ofisy.application.serviceOrderService.exceptions.ServiceOrderServiceNotFoundException;
import br.com.ofisy.domain.service.Service;
import br.com.ofisy.domain.service.ServiceRepository;
import br.com.ofisy.domain.service.exceptions.ServiceNotFoundException;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderService;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderServiceRepository;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderServiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceApplication {

    private final ServiceRepository repository;

    public Service create(ServiceRequestDTO dto) {
        Service service = Service.create(
                dto.name(),
                dto.description(),
                dto.price()
        );

        return repository.save(service);
    }

    public Service findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id.toString()));
    }

    public Page<Service> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Service findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new ServiceNotFoundException(name));
    }
}
