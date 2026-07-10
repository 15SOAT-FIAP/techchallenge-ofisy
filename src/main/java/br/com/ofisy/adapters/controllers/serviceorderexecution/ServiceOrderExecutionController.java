package br.com.ofisy.adapters.controllers.serviceorderexecution;

import br.com.ofisy.adapters.controllers.serviceorderexecution.dto.ServiceOrderExecutionResponseDTO;
import br.com.ofisy.adapters.presenters.serviceorderexecution.ServiceOrderExecutionPresenter;
import br.com.ofisy.application.serviceorderexecution.cancel.CancelServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.createcomplete.CompleteServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.create.CreateServiceOrderExecutionUseCase;
import br.com.ofisy.adapters.controllers.serviceorderexecution.dto.ServiceOrderExecutionRequestDTO;
import br.com.ofisy.application.serviceorderexecution.identifybyid.IdentifyByIdServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.list.ListServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.listbyservicecatalogid.ListByServiceCatalogIdServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.listbyserviceorderid.ListByServiceOrderIdServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.listbystatus.ListByStatusServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.start.StartExecutionUseCase;
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

    private final ListServiceOrderExecutionUseCase listServiceOrderExecutionUseCase;
    private final IdentifyByIdServiceOrderExecutionUseCase identifyByIdServiceOrderExecutionUseCase;
    private final CreateServiceOrderExecutionUseCase createServiceOrderExecutionUseCase;
    private final CompleteServiceOrderExecutionUseCase completeServiceOrderExecutionUseCase;
    private final CancelServiceOrderExecutionUseCase cancelServiceOrderExecutionUseCase;
    private final StartExecutionUseCase startExecutionUseCase;
    private final ListByServiceCatalogIdServiceOrderExecutionUseCase listByServiceCatalogIdUseCase;
    private final ListByStatusServiceOrderExecutionUseCase listByStatusUseCase;
    private final ListByServiceOrderIdServiceOrderExecutionUseCase listByServiceOrderIdUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceOrderExecutionResponseDTO>> getAll(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(listServiceOrderExecutionUseCase.execute(pageable).map(ServiceOrderExecutionPresenter::present));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderExecutionResponseDTO> getById(@PathVariable UUID id) {

        return ResponseEntity.ok(ServiceOrderExecutionPresenter.present(identifyByIdServiceOrderExecutionUseCase.execute(id)));
    }

    @GetMapping(params = "serviceCatalogId")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceOrderExecutionResponseDTO>> getByServiceCatalogId(
            @RequestParam UUID serviceCatalogId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(listByServiceCatalogIdUseCase.execute(serviceCatalogId, pageable).map(ServiceOrderExecutionPresenter::present));
    }

    @GetMapping(params = "status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceOrderExecutionResponseDTO>> getByStatus(
            @RequestParam String status,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(listByStatusUseCase.execute(status, pageable).map(ServiceOrderExecutionPresenter::present));
    }

    @GetMapping("/service_order/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceOrderExecutionResponseDTO>> getByServiceOrderId(@PathVariable UUID id, @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(listByServiceOrderIdUseCase.execute(id, pageable).map(ServiceOrderExecutionPresenter::present));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderExecutionResponseDTO> create(@Valid @RequestBody ServiceOrderExecutionRequestDTO requestDTO) {
        var cmd = new CreateServiceOrderExecutionUseCase.CreateServiceOrderExecutionCommand(
                requestDTO.serviceCatalogId(),
                requestDTO.serviceOrderId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ServiceOrderExecutionPresenter.present(createServiceOrderExecutionUseCase.execute(cmd)));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderExecutionResponseDTO> complete(@PathVariable UUID id) {

        return ResponseEntity.ok(ServiceOrderExecutionPresenter.present(completeServiceOrderExecutionUseCase.execute(id)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderExecutionResponseDTO> cancel(@PathVariable UUID id) {

        return ResponseEntity.ok(ServiceOrderExecutionPresenter.present(cancelServiceOrderExecutionUseCase.execute(id)));
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderExecutionResponseDTO> start(@PathVariable UUID id) {
        return ResponseEntity.ok(ServiceOrderExecutionPresenter.present(startExecutionUseCase.execute(id)));
    }
}
