package br.com.ofisy.adapters.controllers.serviceorder;

import br.com.ofisy.adapters.controllers.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.adapters.controllers.serviceorder.dto.ServiceOrderResponseDTO;
import br.com.ofisy.adapters.controllers.serviceorder.dto.ServiceOrderStatusResponseDTO;
import br.com.ofisy.adapters.presenters.serviceorder.ServiceOrderPresenter;
import br.com.ofisy.adapters.presenters.serviceorder.ServiceOrderStatusPresenter;
import br.com.ofisy.application.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.quote.dto.ReproveQuoteRequestDTO;
import br.com.ofisy.application.serviceorder.ServiceOrderService;
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

    private final ServiceOrderService serviceOrderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<ServiceOrderResponseDTO> receiveServiceOrder(
            @Valid @RequestBody ServiceOrderRequestDTO request,
            @AuthenticationPrincipal UserDetails user) {

        var serviceOrder = serviceOrderService.create(
                request.vehicleId(), request.customerId(), request.report(), user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ServiceOrderPresenter.present(serviceOrder));
    }

    @PostMapping("/{id}/quote")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<QuoteResponseDTO> generateQuote(
            @PathVariable UUID id,
            @Valid @RequestBody CreateQuoteRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderService.generateQuote(id, request));
    }

    @GetMapping("/received")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceOrderResponseDTO>> listReceived(
            @ParameterObject @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderService.listReceived(pageable).map(ServiceOrderPresenter::present));
    }

    @GetMapping("/finished")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<Page<ServiceOrderResponseDTO>> listFinished(
            @ParameterObject @PageableDefault(size = 10, sort = "finishedAt", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(serviceOrderService.listFinished(pageable).map(ServiceOrderPresenter::present));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ServiceOrderStatusResponseDTO> getStatusById(@PathVariable UUID id) {
        return ResponseEntity.ok(ServiceOrderStatusPresenter.present(serviceOrderService.getStatus(id)));
    }

    @PatchMapping("/{id}/start-diagnostic")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderResponseDTO> startDiagnosticServiceOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ServiceOrderPresenter.present(serviceOrderService.startDiagnostic(id)));
    }

    @PatchMapping("/{id}/deliver")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderResponseDTO> deliverToCustomerServiceOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ServiceOrderPresenter.present(serviceOrderService.deliverToCustomer(id)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<ServiceOrderResponseDTO> cancelServiceOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ServiceOrderPresenter.present(serviceOrderService.close(id)));
    }

    @PatchMapping("/quote/{id}/approve")
    public ResponseEntity<QuoteResponseDTO> approveQuote(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceOrderService.approveQuote(id));
    }

    @PatchMapping("/quote/{id}/reprove")
    public ResponseEntity<QuoteResponseDTO> reproveQuote(
            @PathVariable UUID id,
            @RequestBody ReproveQuoteRequestDTO reproveQuoteRequestDTO) {
        return ResponseEntity.ok(serviceOrderService.reproveQuote(id, reproveQuoteRequestDTO));
    }
}