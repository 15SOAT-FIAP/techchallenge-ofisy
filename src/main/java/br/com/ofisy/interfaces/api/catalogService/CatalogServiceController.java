package br.com.ofisy.interfaces.api.catalogService;

import br.com.ofisy.domain.catalogService.CatalogService;
import br.com.ofisy.domain.catalogService.CatalogServiceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalog-services")
@RequiredArgsConstructor
public class CatalogServiceController {

    private final CatalogServiceRepository repository;

    @GetMapping
    public ResponseEntity<Page<CatalogService>> getAllCatalogServices(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(repository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogService> getCatalogServiceById(@PathVariable UUID id) {

        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(params = "name")
    public ResponseEntity<CatalogService> getCatalogServiceByName(@RequestParam String name) {

        return repository.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CatalogService> createCatalogService(
            @Valid @RequestBody CatalogServiceCreateRequest request) {

        var service = CatalogService.create(
                request.name(),
                request.description(),
                request.price()
        );

        var saved = repository.save(service);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public record CatalogServiceCreateRequest(
            String name,
            String description,
            BigDecimal price
    ) {}
}

