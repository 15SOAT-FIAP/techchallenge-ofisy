package br.com.ofisy.interfaces.api.service;

import br.com.ofisy.application.service.ServiceApplicationService;
import br.com.ofisy.application.service.dto.ServiceRequestDTO;
import br.com.ofisy.domain.service.Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceApplicationService serviceService;

    @GetMapping
    public ResponseEntity<Page<Service>> getAllServices(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Service> getServiceById(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceService.findById(id));
    }

    @GetMapping(params = "customerId")
    public ResponseEntity<Page<Service>> getServicesByCustomerId(
            @RequestParam UUID customerId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceService.findByCustomerId(customerId, pageable));
    }

    @GetMapping(params = "catalogServiceId")
    public ResponseEntity<Page<Service>> getServicesByCatalogServiceId(
            @RequestParam UUID catalogServiceId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceService.findByCatalogServiceId(catalogServiceId, pageable));
    }

    @GetMapping(params = "status")
    public ResponseEntity<Page<Service>> getServicesByStatus(
            @RequestParam String status,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceService.findByStatus(status, pageable));
    }

    @PostMapping
    public ResponseEntity<Service> createService(@Valid @RequestBody ServiceRequestDTO requestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED).body(serviceService.create(requestDTO));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Service> completeService(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceService.completeService(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Service> cancelService(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceService.cancelService(id));
    }
}

