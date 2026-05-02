package br.com.ofisy.interfaces.api.servicecatalog;

import br.com.ofisy.application.servicecatalog.ServiceCatalogService;
import br.com.ofisy.application.servicecatalog.dto.ServiceCatalogRequestDTO;
import br.com.ofisy.application.servicecatalog.dto.ServiceCatalogResponseDTO;
import br.com.ofisy.application.serviceorderexecution.ServiceOrderExecutionService;
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
@RequestMapping("/api/v1/services-catalog")
@RequiredArgsConstructor
public class ServiceCatalogController implements ServiceCatalogApi {
    private final ServiceCatalogService serviceCatalogService;
    private final ServiceOrderExecutionService serviceOrderExecutionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceCatalogResponseDTO>> getAll(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(serviceCatalogService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceCatalogResponseDTO> getById(@PathVariable UUID id) {

        return ResponseEntity.status(HttpStatus.OK).body(serviceCatalogService.findById(id));

    }

    @GetMapping(params = "name")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceCatalogResponseDTO> getByName(@RequestParam String name) {

        return ResponseEntity.ok(serviceCatalogService.findByName(name));

    }

    @GetMapping("execution_time_average/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Double> getExecutionTimeAverage(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceOrderExecutionService.getAverageExecutionTimeByService(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceCatalogResponseDTO> create(
            @Valid @RequestBody ServiceCatalogRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(serviceCatalogService.create(dto));
    }
}
