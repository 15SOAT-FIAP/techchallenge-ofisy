package br.com.ofisy.application.serviceorder;

import br.com.ofisy.application.notification.NotificationService;
import br.com.ofisy.application.notification.dto.QuoteNotificationRequestDTO;
import br.com.ofisy.application.quote.QuoteService;
import br.com.ofisy.application.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.quote.dto.ReproveQuoteRequestDTO;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderResponseDTO;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderStatusResponseDTO;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.customer.identifybyid.IdentifyByIdCustomerUseCase;
import br.com.ofisy.application.user.UserService;
import br.com.ofisy.application.vehicle.VehicleService;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
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
    private final VehicleService vehicleService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final QuoteService quoteService;
    private final ServiceOrderFinalizationService finalizationService;

    @Transactional
    public ServiceOrderResponseDTO create(ServiceOrderRequestDTO request, String createdByEmail) {
        identifyByIdCustomerUseCase.execute(request.customerId());
        var vehicle = vehicleService.identifyVehicleById(request.vehicleId());
        if (!vehicle.customerId().equals(request.customerId())) {
            throw new VehicleNotOwnedByCustomerException(request.vehicleId(), request.customerId());
        }

        var createdBy = userService.getIdByEmail(createdByEmail);
        var serviceOrder = ServiceOrderMapper.toDomain(request, createdBy);
        var receivedServiceOrder = serviceOrderRepository.save(serviceOrder);
        return ServiceOrderMapper.toResponseDTO(receivedServiceOrder);
    }

    @Transactional
    public ServiceOrderResponseDTO close(UUID id) {
        var serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));

        finalizationService.cancelPending(id);
        serviceOrder.cancel();
        return ServiceOrderMapper.toResponseDTO(serviceOrderRepository.save(serviceOrder));
    }

    @Transactional(readOnly = true)
    public Page<ServiceOrderResponseDTO> listReceived(Pageable pageable) {
        return serviceOrderRepository.findByStatus(ServiceOrderStatus.RECEIVED, pageable)
                .map(ServiceOrderMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ServiceOrderResponseDTO> listFinished(Pageable pageable) {
        return serviceOrderRepository.findByStatus(ServiceOrderStatus.FINISHED, pageable)
                .map(ServiceOrderMapper::toResponseDTO);
    }

    @Transactional
    public ServiceOrderResponseDTO startDiagnostic(UUID id) {
        var serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        serviceOrder.startDiagnostic();
        return ServiceOrderMapper.toResponseDTO(serviceOrderRepository.save(serviceOrder));
    }

    @Transactional
    public QuoteResponseDTO generateQuote(UUID id, CreateQuoteRequestDTO request) {
        var serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        var quote = quoteService.create(id, request);
        serviceOrder.sendToApproval();
        serviceOrderRepository.save(serviceOrder);
        var quoteNotification = new QuoteNotificationRequestDTO(quote.id(), id, quote.totalPrice());
        notificationService.createQuoteNotification(quoteNotification);
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
    public ServiceOrderResponseDTO deliverToCustomer(UUID id) {
        var serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        serviceOrder.deliver();
        return ServiceOrderMapper.toResponseDTO(serviceOrderRepository.save(serviceOrder));
    }

    @Transactional(readOnly = true)
    public ServiceOrderStatusResponseDTO getStatus(UUID id) {
        var serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        return ServiceOrderMapper.toStatusResponseDTO(serviceOrder);
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
