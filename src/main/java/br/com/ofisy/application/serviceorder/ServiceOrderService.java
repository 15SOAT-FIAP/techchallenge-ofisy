package br.com.ofisy.application.serviceorder;

import br.com.ofisy.application.notification.createquote.CreateQuoteNotificationUseCase;
import br.com.ofisy.application.quote.QuoteService;
import br.com.ofisy.application.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.quote.dto.ReproveQuoteRequestDTO;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.customer.identifybyid.IdentifyByIdCustomerUseCase;
import br.com.ofisy.application.user.UserService;
import br.com.ofisy.application.vehicle.identifybyid.IdentifyVehicleByIdUseCase;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.vehicle.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final IdentifyByIdCustomerUseCase identifyByIdCustomerUseCase;
    private final IdentifyVehicleByIdUseCase identifyVehicleByIdUseCase;
    private final UserService userService;
    private final CreateQuoteNotificationUseCase createQuoteNotificationUseCase;
    private final QuoteService quoteService;
    private final ServiceOrderFinalizationService finalizationService;

    @Transactional
    public ServiceOrder create(UUID vehicleId, UUID customerId, String report, String createdByEmail) {
        identifyByIdCustomerUseCase.execute(customerId);
        Vehicle vehicle = identifyVehicleByIdUseCase.execute(vehicleId);
        if (!vehicle.getCustomerId().equals(customerId)) {
            throw new VehicleNotOwnedByCustomerException(vehicleId, customerId);
        }
        var createdBy = userService.getIdByEmail(createdByEmail);
        var serviceOrder = ServiceOrder.receive(vehicleId, customerId, report, createdBy);
        return serviceOrderRepository.save(serviceOrder);
    }

    @Transactional
    public ServiceOrder close(UUID id) {
        var serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        finalizationService.cancelPending(id);
        serviceOrder.cancel();
        return serviceOrderRepository.save(serviceOrder);
    }

    @Transactional(readOnly = true)
    public Page<ServiceOrder> listReceived(Pageable pageable) {
        return serviceOrderRepository.findByStatus(ServiceOrderStatus.RECEIVED, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ServiceOrder> listFinished(Pageable pageable) {
        return serviceOrderRepository.findByStatus(ServiceOrderStatus.FINISHED, pageable);
    }

    @Transactional
    public ServiceOrder startDiagnostic(UUID id) {
        var serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        serviceOrder.startDiagnostic();
        return serviceOrderRepository.save(serviceOrder);
    }

    @Transactional
    public QuoteResponseDTO generateQuote(UUID id, CreateQuoteRequestDTO request) {
        var serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        var quote = quoteService.create(id, request);
        serviceOrder.sendToApproval();
        serviceOrderRepository.save(serviceOrder);
        createQuoteNotificationUseCase.execute(
                new CreateQuoteNotificationUseCase.CreateQuoteCommand(
                        quote.id(),
                        id,
                        quote.totalPrice()
                )
        );
        return quote;
    }

    @Transactional
    public void startExecution(UUID id) {
        var serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        if (serviceOrder.getStatus() == ServiceOrderStatus.AWAITING_EXECUTION) {
            serviceOrder.startExecution();
            serviceOrderRepository.save(serviceOrder);
        }
    }

    @Transactional
    public ServiceOrder deliverToCustomer(UUID id) {
        var serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        serviceOrder.deliver();
        return serviceOrderRepository.save(serviceOrder);
    }

    @Transactional(readOnly = true)
    public ServiceOrder getStatus(UUID id) {
        return serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
    }

    @Transactional
    public QuoteResponseDTO approveQuote(UUID quoteId) {
        QuoteResponseDTO quoteResponseDTO = quoteService.approve(quoteId);
        var serviceOrder = serviceOrderRepository.findById(quoteResponseDTO.serviceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(quoteResponseDTO.serviceOrderId()));
        serviceOrder.approve();
        serviceOrderRepository.save(serviceOrder);
        return quoteResponseDTO;
    }

    @Transactional
    public QuoteResponseDTO reproveQuote(UUID quoteId, ReproveQuoteRequestDTO requestDTO) {
        QuoteResponseDTO quoteResponseDTO = quoteService.reprove(quoteId, requestDTO);
        var serviceOrder = serviceOrderRepository.findById(quoteResponseDTO.serviceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(quoteResponseDTO.serviceOrderId()));
        serviceOrder.cancel();
        serviceOrderRepository.save(serviceOrder);
        return quoteResponseDTO;
    }
}