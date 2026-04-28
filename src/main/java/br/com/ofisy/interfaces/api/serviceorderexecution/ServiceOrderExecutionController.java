package br.com.ofisy.interfaces.api.serviceorderexecution;

import br.com.ofisy.application.serviceorderexecution.ServiceOrderExecutionService;
import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionRequestDTO;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
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
@RequestMapping("/api/v1/service_order_executions")
@RequiredArgsConstructor
public class ServiceOrderExecutionController implements ServiceOrderExecutionApi {

    private final ServiceOrderExecutionService serviceOrderExecutionService;

    @GetMapping
    public ResponseEntity<Page<ServiceOrderExecution>> getAll(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderExecutionService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrderExecution> getById(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceOrderExecutionService.findById(id));
    }

    @GetMapping(params = "serviceCatalogId")
    public ResponseEntity<Page<ServiceOrderExecution>> getByServiceCatalogId(
            @RequestParam UUID serviceCatalogId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderExecutionService.findByServiceCatalogId(serviceCatalogId, pageable));
    }

    @GetMapping(params = "status")
    public ResponseEntity<Page<ServiceOrderExecution>> getByStatus(
            @RequestParam String status,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderExecutionService.findByStatus(status, pageable));
    }

    @GetMapping("/service_order/{id}")
    public ResponseEntity<Page<ServiceOrderExecution>> getByServiceOrderId(@PathVariable UUID id, @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderExecutionService.findByServiceOrderId(id, pageable));
    }

    @PostMapping
    public ResponseEntity<ServiceOrderExecution> create(@Valid @RequestBody ServiceOrderExecutionRequestDTO requestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderExecutionService.create(requestDTO));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ServiceOrderExecution> complete(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceOrderExecutionService.complete(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ServiceOrderExecution> cancel(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceOrderExecutionService.cancel(id));
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<ServiceOrderExecution> start(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceOrderExecutionService.start(id));
    }
}
