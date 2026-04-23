package br.com.ofisy.interfaces.api.serviceOrderService;

import br.com.ofisy.application.serviceOrderService.ServiceOrderServiceApplication;
import br.com.ofisy.application.serviceOrderService.dto.ServiceOrderServiceRequestDTO;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderService;
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
@RequestMapping("/api/v1/service_order_services")
@RequiredArgsConstructor
public class ServiceOrderServiceController {

    private final ServiceOrderServiceApplication serviceOrderServiceApplication;

    @GetMapping
    public ResponseEntity<Page<ServiceOrderService>> getAll(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderServiceApplication.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrderService> getById(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceOrderServiceApplication.findById(id));
    }

    @GetMapping(params = "serviceId")
    public ResponseEntity<Page<ServiceOrderService>> getByServiceId(
            @RequestParam UUID catalogServiceId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderServiceApplication.findByServiceId(catalogServiceId, pageable));
    }

    @GetMapping(params = "status")
    public ResponseEntity<Page<ServiceOrderService>> getByStatus(
            @RequestParam String status,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderServiceApplication.findByStatus(status, pageable));
    }

    @GetMapping("/service_order/{id}")
    public ResponseEntity<Page<ServiceOrderService>> getByServiceOrderId(@PathVariable UUID id, @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderServiceApplication.findByServiceOrderId(id, pageable));
    }

    @PostMapping
    public ResponseEntity<ServiceOrderService> create(@Valid @RequestBody ServiceOrderServiceRequestDTO requestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderServiceApplication.create(requestDTO));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ServiceOrderService> complete(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceOrderServiceApplication.complete(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ServiceOrderService> cancel(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceOrderServiceApplication.cancel(id));
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<ServiceOrderService> start(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceOrderServiceApplication.start(id));
    }
}

