package br.com.ofisy.adapters.controllers.servicecatalog;

import br.com.ofisy.adapters.controllers.servicecatalog.dto.ServiceCatalogRequestDTO;
import br.com.ofisy.adapters.controllers.servicecatalog.dto.ServiceCatalogResponseDTO;
import br.com.ofisy.adapters.presenters.servicecatalog.ServiceCatalogPresenter;
import br.com.ofisy.application.servicecatalog.create.CreateServiceCatalogUseCase;
import br.com.ofisy.application.servicecatalog.identifybyid.IdentifyByIdServiceCatalogUseCase;
import br.com.ofisy.application.servicecatalog.identifybyname.IdentifyByNameServiceCatalogUseCase;
import br.com.ofisy.application.servicecatalog.list.ListServiceCatalogUseCase;
import br.com.ofisy.application.serviceorderexecution.getaverageexecutiontime.GetAverageExecutionTimeServiceOrderExecutionUseCase;
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
    private final GetAverageExecutionTimeServiceOrderExecutionUseCase getAverageExecutionTimeUseCase;
    private final CreateServiceCatalogUseCase createServiceCatalogUseCase;
    private final ListServiceCatalogUseCase listServiceCatalogUseCase;
    private final IdentifyByIdServiceCatalogUseCase identifyByIdServiceCatalogUseCase;
    private final IdentifyByNameServiceCatalogUseCase identifyByNameServiceCatalogUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceCatalogResponseDTO>> getAll(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(listServiceCatalogUseCase.execute(pageable).map(ServiceCatalogPresenter::present));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceCatalogResponseDTO> getById(@PathVariable UUID id) {

        return ResponseEntity.ok(ServiceCatalogPresenter.present(identifyByIdServiceCatalogUseCase.execute(id)));
    }

    @GetMapping(params = "name")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceCatalogResponseDTO> getByName(@RequestParam String name) {

        return ResponseEntity.ok(ServiceCatalogPresenter.present(identifyByNameServiceCatalogUseCase.execute(name)));

    }

    @GetMapping("execution_time_average/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Double> getExecutionTimeAverage(@PathVariable UUID id) {

        return ResponseEntity.ok(getAverageExecutionTimeUseCase.execute(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceCatalogResponseDTO> create(
            @Valid @RequestBody ServiceCatalogRequestDTO dto) {
        CreateServiceCatalogUseCase.CreateServiceCatalogCommand cmd = new CreateServiceCatalogUseCase.CreateServiceCatalogCommand(
                dto.name(), dto.description(), dto.price());
        return ResponseEntity.status(HttpStatus.CREATED).body(ServiceCatalogPresenter.present(createServiceCatalogUseCase.execute(cmd)));
    }
}
