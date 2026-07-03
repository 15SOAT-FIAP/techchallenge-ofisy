package br.com.ofisy.adapters.controllers.serviceorder;

import br.com.ofisy.adapters.controllers.quote.dto.UpdateQuoteRequestDTO;
import br.com.ofisy.adapters.controllers.serviceorder.dto.*;
import br.com.ofisy.adapters.presenters.quote.QuotePresenter;
import br.com.ofisy.adapters.presenters.serviceorder.ServiceOrderPresenter;
import br.com.ofisy.adapters.presenters.serviceorder.ServiceOrderStatusPresenter;
import br.com.ofisy.adapters.controllers.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.adapters.controllers.quote.dto.QuoteResponseDTO;
import br.com.ofisy.adapters.controllers.quote.dto.ReproveQuoteRequestDTO;
import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.application.serviceorder.approvequote.ApproveServiceOrderQuoteUseCase;
import br.com.ofisy.application.serviceorder.cancel.CancelServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.create.CreateServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.createcomplete.CreateCompleteServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.delivertocustomer.DeliverToCustomerUseCase;
import br.com.ofisy.application.serviceorder.generatequote.GenerateServiceOrderQuoteUseCase;
import br.com.ofisy.application.serviceorder.getstatus.GetServiceOrderStatusUseCase;
import br.com.ofisy.application.serviceorder.listactive.ListActiveServiceOrdersUseCase;
import br.com.ofisy.application.serviceorder.listfinished.ListFinishedServiceOrdersUseCase;
import br.com.ofisy.application.serviceorder.listreceived.ListReceivedServiceOrdersUseCase;
import br.com.ofisy.application.serviceorder.reprovequote.ReproveServiceOrderQuoteUseCase;
import br.com.ofisy.application.serviceorder.startdiagnostic.StartDiagnosticUseCase;
import br.com.ofisy.application.serviceorder.updatequote.UpdateQuoteUseCase;
import br.com.ofisy.domain.quote.Quote;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController implements ServiceOrderApi {

    private final CreateServiceOrderUseCase createServiceOrderUseCase;
    private final CreateCompleteServiceOrderUseCase createCompleteServiceOrderUseCase;
    private final CancelServiceOrderUseCase cancelServiceOrderUseCase;
    private final ListReceivedServiceOrdersUseCase listReceivedServiceOrdersUseCase;
    private final ListFinishedServiceOrdersUseCase listFinishedServiceOrdersUseCase;
    private final ListActiveServiceOrdersUseCase listActiveServiceOrdersUseCase;
    private final StartDiagnosticUseCase startDiagnosticUseCase;
    private final DeliverToCustomerUseCase deliverToCustomerUseCase;
    private final GetServiceOrderStatusUseCase getServiceOrderStatusUseCase;
    private final GenerateServiceOrderQuoteUseCase generateServiceOrderQuoteUseCase;
    private final ApproveServiceOrderQuoteUseCase approveServiceOrderQuoteUseCase;
    private final ReproveServiceOrderQuoteUseCase reproveServiceOrderQuoteUseCase;
    private final UpdateQuoteUseCase updateQuoteUseCase;

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

    @PostMapping("/create-complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<CreateServiceOrderResponseDTO> receiveCompleteServiceOrder(
            @Valid @RequestBody CreateServiceOrderRequestDTO request,
            @AuthenticationPrincipal UserDetails user) {

        var stockItems = request.stockItems() == null ? List.<CreateQuoteUseCase.StockItemCommand>of()
                : request.stockItems().stream()
                .map(stockItem -> new CreateQuoteUseCase.StockItemCommand(stockItem.stockId(), stockItem.quantity()))
                .toList();

        var serviceItems = request.serviceItems() == null ? List.<CreateQuoteUseCase.ServiceItemCommand>of()
                : request.serviceItems().stream()
                .map(serviceItem -> new CreateQuoteUseCase.ServiceItemCommand(serviceItem.serviceCatalogId()))
                .toList();

        ServiceOrder serviceOrder = createCompleteServiceOrderUseCase.execute(
                new CreateCompleteServiceOrderUseCase.CreateServiceOrderCommand(
                        request.vehicleId(), request.customerId(), request.report(),
                        user.getUsername(), stockItems, serviceItems));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateServiceOrderResponseDTO(serviceOrder.getId()));
    }

    @PostMapping("/{id}/quote")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<QuoteResponseDTO> generateQuote(
            @PathVariable UUID id,
            @Valid @RequestBody CreateQuoteRequestDTO request) {

        Quote quote = generateServiceOrderQuoteUseCase.execute(
                new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                        id, toStockItemCommands(request), toServiceItemCommands(request)));

        return ResponseEntity.status(HttpStatus.CREATED).body(QuotePresenter.present(quote));
    }

    @PatchMapping("/{id}/quote")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT', 'MECHANIC')")
    public ResponseEntity<QuoteResponseDTO> updateQuote(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQuoteRequestDTO request) {

        var stockItems = request.stockItems() == null ? List.<CreateQuoteUseCase.StockItemCommand>of()
                : request.stockItems().stream()
                .map(i -> new CreateQuoteUseCase.StockItemCommand(i.stockId(), i.quantity()))
                .toList();

        var serviceItems = request.serviceItems() == null ? List.<CreateQuoteUseCase.ServiceItemCommand>of()
                : request.serviceItems().stream()
                .map(i -> new CreateQuoteUseCase.ServiceItemCommand(i.serviceCatalogId()))
                .toList();

        Quote quote = updateQuoteUseCase.execute(
                new UpdateQuoteUseCase.UpdateQuoteCommand(id, stockItems, serviceItems));

        return ResponseEntity.ok(QuotePresenter.present(quote));
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

    @PatchMapping("/quote/{id}/approve")
    public ResponseEntity<QuoteResponseDTO> approveQuote(@PathVariable UUID id) {
        Quote quote = approveServiceOrderQuoteUseCase.execute(id);
        return ResponseEntity.ok(QuotePresenter.present(quote));
    }

    @PatchMapping("/quote/{id}/reprove")
    public ResponseEntity<QuoteResponseDTO> reproveQuote(
            @PathVariable UUID id,
            @RequestBody ReproveQuoteRequestDTO reproveQuoteRequestDTO) {

        Quote quote = reproveServiceOrderQuoteUseCase.execute(
                new ReproveServiceOrderQuoteUseCase.ReproveServiceOrderQuoteCommand(id, reproveQuoteRequestDTO.reason()));
        return ResponseEntity.ok(QuotePresenter.present(quote));
    }

    private List<CreateQuoteUseCase.StockItemCommand> toStockItemCommands(CreateQuoteRequestDTO request) {
        if (request.stockItems() == null) {
            return List.of();
        }
        return request.stockItems().stream()
                .map(stockItem -> new CreateQuoteUseCase.StockItemCommand(stockItem.stockId(), stockItem.quantity()))
                .toList();
    }

    private List<CreateQuoteUseCase.ServiceItemCommand> toServiceItemCommands(CreateQuoteRequestDTO request) {
        if (request.serviceItems() == null) {
            return List.of();
        }
        return request.serviceItems().stream()
                .map(serviceItem -> new CreateQuoteUseCase.ServiceItemCommand(serviceItem.serviceCatalogId()))
                .toList();
    }
}