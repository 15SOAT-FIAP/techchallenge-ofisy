package br.com.ofisy.interfaces.api.serviceorder;

import br.com.ofisy.application.serviceorder.ServiceOrderService;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ServiceOrderResponseDTO> receiveServiceOrder(
            @Valid @RequestBody ServiceOrderRequestDTO request,
            @AuthenticationPrincipal UserDetails user) {

        return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrderService.create(request, user.getUsername()));
    }

    @GetMapping("/received")
    public ResponseEntity<Page<ServiceOrderResponseDTO>> listReceived(
    @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(serviceOrderService.listReceived(pageable));
    }

    @GetMapping("/finished")
    public ResponseEntity<Page<ServiceOrderResponseDTO>> listFinished(
            @ParameterObject @PageableDefault(size = 10, sort = "finishedAt", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(serviceOrderService.listFinished(pageable));
    }

    @PatchMapping("/{id}/start-diagnostic")
    public ResponseEntity<ServiceOrderResponseDTO> startDiagnosticServiceOrder(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(serviceOrderService.startDiagnostic(id));
    }

    @PatchMapping("/{id}/deliver")
    public ResponseEntity<ServiceOrderResponseDTO> deliverToCustomerServiceOrder(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(serviceOrderService.deliverToCustomer(id));
    }
}
