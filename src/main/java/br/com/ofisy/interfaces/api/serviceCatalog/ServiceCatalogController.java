package br.com.ofisy.interfaces.api.serviceCatalog;

import br.com.ofisy.application.serviceCatalog.ServiceCatalogService;
import br.com.ofisy.application.serviceCatalog.dto.ServiceCatalogRequestDTO;
import br.com.ofisy.application.serviceOrderExecution.ServiceOrderExecutionService;
import br.com.ofisy.domain.serviceCatalog.ServiceCatalog;
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
@RequestMapping("/api/v1/services-catalog")
@RequiredArgsConstructor
public class ServiceCatalogController implements ServiceCatalogApi {
    private final ServiceCatalogService serviceCatalogService;
    private final ServiceOrderExecutionService serviceOrderExecutionService;

    @GetMapping
    public ResponseEntity<Page<ServiceCatalog>> getAll(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(serviceCatalogService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceCatalog> getById(@PathVariable UUID id) {

        return ResponseEntity.status(HttpStatus.OK).body(serviceCatalogService.findById(id));

    }

    @GetMapping(params = "name")
    public ResponseEntity<ServiceCatalog> getByName(@RequestParam String name) {

        return ResponseEntity.ok(serviceCatalogService.findByName(name));

    }

    @GetMapping("execution_time_average/{id}")
    public ResponseEntity<Double> getExecutionTimeAverage(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceOrderExecutionService.getAverageExecutionTimeByService(id));
    }

    @PostMapping
    public ResponseEntity<ServiceCatalog> create(
            @Valid @RequestBody ServiceCatalogRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(serviceCatalogService.create(dto));
    }
}
