package br.com.ofisy.interfaces.api.serviceorderexecution;

import br.com.ofisy.application.serviceorderexecution.ServiceOrderExecutionService;
import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionRequestDTO;
import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/service_order_executions")
@RequiredArgsConstructor
public class ServiceOrderExecutionController implements ServiceOrderExecutionApi {

    private final ServiceOrderExecutionService serviceOrderExecutionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceOrderExecutionResponseDTO>> getAll(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderExecutionService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderExecutionResponseDTO> getById(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceOrderExecutionService.findById(id));
    }

    @GetMapping(params = "serviceCatalogId")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceOrderExecutionResponseDTO>> getByServiceCatalogId(
            @RequestParam UUID serviceCatalogId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderExecutionService.findByServiceCatalogId(serviceCatalogId, pageable));
    }

    @GetMapping(params = "status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceOrderExecutionResponseDTO>> getByStatus(
            @RequestParam String status,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderExecutionService.findByStatus(status, pageable));
    }

    @GetMapping("/service_order/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceOrderExecutionResponseDTO>> getByServiceOrderId(@PathVariable UUID id, @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderExecutionService.findByServiceOrderId(id, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderExecutionResponseDTO> create(@Valid @RequestBody ServiceOrderExecutionRequestDTO requestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderExecutionService.create(requestDTO));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderExecutionResponseDTO> complete(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceOrderExecutionService.complete(id));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderExecutionResponseDTO> cancel(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceOrderExecutionService.cancel(id));
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderExecutionResponseDTO> start(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceOrderExecutionService.start(id));
    }
}
