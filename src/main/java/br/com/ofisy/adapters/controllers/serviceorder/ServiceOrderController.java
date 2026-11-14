package br.com.ofisy.adapters.controllers.serviceorder;

import br.com.ofisy.adapters.controllers.serviceorder.dto.*;
import br.com.ofisy.adapters.presenters.serviceorder.ServiceOrderPresenter;
import br.com.ofisy.adapters.presenters.serviceorder.ServiceOrderStatusPresenter;
import br.com.ofisy.application.serviceorder.cancel.CancelServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.create.CreateServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.delivertocustomer.DeliverToCustomerUseCase;
import br.com.ofisy.application.serviceorder.getstatus.GetServiceOrderStatusUseCase;
import br.com.ofisy.application.serviceorder.listactive.ListActiveServiceOrdersUseCase;
import br.com.ofisy.application.serviceorder.listfinished.ListFinishedServiceOrdersUseCase;
import br.com.ofisy.application.serviceorder.listreceived.ListReceivedServiceOrdersUseCase;
import br.com.ofisy.application.serviceorder.startdiagnostic.StartDiagnosticUseCase;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController implements ServiceOrderApi {

    private final CreateServiceOrderUseCase createServiceOrderUseCase;
    private final CancelServiceOrderUseCase cancelServiceOrderUseCase;
    private final ListReceivedServiceOrdersUseCase listReceivedServiceOrdersUseCase;
    private final ListFinishedServiceOrdersUseCase listFinishedServiceOrdersUseCase;
    private final ListActiveServiceOrdersUseCase listActiveServiceOrdersUseCase;
    private final StartDiagnosticUseCase startDiagnosticUseCase;
    private final DeliverToCustomerUseCase deliverToCustomerUseCase;
    private final GetServiceOrderStatusUseCase getServiceOrderStatusUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<ServiceOrderResponseDTO> receiveServiceOrder(
            @Valid @RequestBody ServiceOrderRequestDTO request,
            @AuthenticationPrincipal UserDetails user) {

        ServiceOrder serviceOrder = createServiceOrderUseCase.execute(
                new CreateServiceOrderUseCase.CreateServiceOrderCommand(
                        request.vehicleId(), request.customerId(), request.report(), user.getUsername()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ServiceOrderPresenter.present(serviceOrder));
    }

    @GetMapping("/received")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceOrderResponseDTO>> listReceived(
            @ParameterObject @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(listReceivedServiceOrdersUseCase.execute(pageable).map(ServiceOrderPresenter::present));
    }

    @GetMapping("/finished")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceOrderResponseDTO>> listFinished(
            @ParameterObject @PageableDefault(size = 10, sort = "finishedAt", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(listFinishedServiceOrdersUseCase.execute(pageable).map(ServiceOrderPresenter::present));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceOrderResponseDTO>> listActive(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(listActiveServiceOrdersUseCase.execute(pageable).map(ServiceOrderPresenter::present));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ServiceOrderStatusResponseDTO> getStatusById(@PathVariable UUID id) {
        return ResponseEntity.ok(ServiceOrderStatusPresenter.present(getServiceOrderStatusUseCase.execute(id)));
    }

    @Override
    @GetMapping("/{id}/customer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceOrderCustomerResponseDTO> getCustomerById(@PathVariable UUID id) {
        var order = getServiceOrderStatusUseCase.execute(id);
        return ResponseEntity.ok(new ServiceOrderCustomerResponseDTO(order.getCustomerId()));
    }

    @PatchMapping("/{id}/start-diagnostic")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderResponseDTO> startDiagnosticServiceOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ServiceOrderPresenter.present(startDiagnosticUseCase.execute(id)));
    }

    @PatchMapping("/{id}/deliver")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderResponseDTO> deliverToCustomerServiceOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ServiceOrderPresenter.present(deliverToCustomerUseCase.execute(id)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderResponseDTO> cancelServiceOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ServiceOrderPresenter.present(cancelServiceOrderUseCase.execute(id)));
    }
}
