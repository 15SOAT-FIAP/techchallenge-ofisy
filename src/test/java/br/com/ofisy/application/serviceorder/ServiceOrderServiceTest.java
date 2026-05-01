package br.com.ofisy.application.serviceorder;

import br.com.ofisy.application.customer.CustomerService;
import br.com.ofisy.application.customer.dto.CustomerResponseDTO;
import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.notification.NotificationService;
import br.com.ofisy.application.quote.QuoteService;
import br.com.ofisy.application.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.user.UserService;
import br.com.ofisy.application.user.exceptions.EmailNotFoundException;
import br.com.ofisy.application.vehicle.VehicleService;
import br.com.ofisy.application.vehicle.dto.VehicleResponseDTO;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final UUID VALID_SERVICE_ORDER_ID = UUID.randomUUID();
    private static final String VALID_EMAIL = "mecanico@ofisy.com";
    private static final String VALID_REPORT = "Barulho na suspensão dianteira";

    @Mock
    private ServiceOrderRepository serviceOrderRepository;
    @Mock
    private CustomerService customerService;
    @Mock
    private VehicleService vehicleService;
    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private QuoteService quoteService;

    @InjectMocks
    private ServiceOrderService serviceOrderService;

    @Nested
    class CreateServiceOrder {

        @Test
        void shouldCreateServiceOrderSuccessfully() {
            when(vehicleService.identifyVehicleById(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByCustomer());
            when(userService.getIdByEmail(VALID_EMAIL)).thenReturn(VALID_USER_ID);
            when(serviceOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var result = serviceOrderService.create(validRequest(), VALID_EMAIL);

            assertThat(result).isNotNull();
            assertThat(result.vehicleId()).isEqualTo(VALID_VEHICLE_ID);
            assertThat(result.customerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(result.report()).isEqualTo(VALID_REPORT);
            assertThat(result.status()).isEqualTo("RECEIVED");
            verify(serviceOrderRepository).save(any());
        }

        @Test
        void shouldCreateServiceOrderWithNullReport() {
            var request = new ServiceOrderRequestDTO(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, null);
            when(vehicleService.identifyVehicleById(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByCustomer());
            when(userService.getIdByEmail(VALID_EMAIL)).thenReturn(VALID_USER_ID);
            when(serviceOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var result = serviceOrderService.create(request, VALID_EMAIL);

            assertThat(result.report()).isNull();
            verify(serviceOrderRepository).save(any());
        }

        @Test
        void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
            doThrow(new CustomerNotFoundException(VALID_CUSTOMER_ID))
                    .when(customerService).identifyCustomerById(VALID_CUSTOMER_ID);

            var request = validRequest();
            assertThatThrownBy(() -> serviceOrderService.create(request, VALID_EMAIL))
                    .isInstanceOf(CustomerNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }

        @Test
        void shouldThrowVehicleNotFoundExceptionWhenVehicleDoesNotExist() {
            doThrow(new VehicleNotFoundException(VALID_VEHICLE_ID))
                    .when(vehicleService).identifyVehicleById(VALID_VEHICLE_ID);

            var request = validRequest();
            assertThatThrownBy(() -> serviceOrderService.create(request, VALID_EMAIL))
                    .isInstanceOf(VehicleNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }

        @Test
        void shouldThrowVehicleNotOwnedByCustomerExceptionWhenOwnershipMismatch() {
            when(vehicleService.identifyVehicleById(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByOther());

            var request = validRequest();
            assertThatThrownBy(() -> serviceOrderService.create(request, VALID_EMAIL))
                    .isInstanceOf(VehicleNotOwnedByCustomerException.class)
                    .hasMessageContaining(VALID_VEHICLE_ID.toString())
                    .hasMessageContaining(VALID_CUSTOMER_ID.toString());

            verify(serviceOrderRepository, never()).save(any());
        }

        @Test
        void shouldThrowEmailNotFoundExceptionWhenUserDoesNotExist() {
            when(vehicleService.identifyVehicleById(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByCustomer());
            doThrow(new EmailNotFoundException(VALID_EMAIL))
                    .when(userService).getIdByEmail(VALID_EMAIL);

            var request = validRequest();
            assertThatThrownBy(() -> serviceOrderService.create(request, VALID_EMAIL))
                    .isInstanceOf(EmailNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }
    }

    @Nested
    class ListReceivedServiceOrders {

        @Test
        void shouldReturnMappedPageOfReceivedServiceOrders() {
            var pageable = PageRequest.of(0, 10);
            var serviceOrder = receivedServiceOrder();
            Page<ServiceOrder> page = new PageImpl<>(List.of(serviceOrder), pageable, 1);
            when(serviceOrderRepository.findByStatus(ServiceOrderStatus.RECEIVED, pageable)).thenReturn(page);

            var result = serviceOrderService.listReceived(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).status()).isEqualTo("RECEIVED");
            assertThat(result.getContent().get(0).vehicleId()).isEqualTo(VALID_VEHICLE_ID);
            assertThat(result.getContent().get(0).customerId()).isEqualTo(VALID_CUSTOMER_ID);
        }

        @Test
        void shouldReturnEmptyPageWhenNoReceivedServiceOrders() {
            var pageable = PageRequest.of(0, 10);
            when(serviceOrderRepository.findByStatus(ServiceOrderStatus.RECEIVED, pageable))
                    .thenReturn(Page.empty(pageable));

            var result = serviceOrderService.listReceived(pageable);

            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        void shouldQueryRepositoryWithReceivedStatusAndProvidedPageable() {
            var pageable = PageRequest.of(2, 5);
            when(serviceOrderRepository.findByStatus(any(), any())).thenReturn(Page.empty(pageable));

            serviceOrderService.listReceived(pageable);

            var statusCaptor = ArgumentCaptor.forClass(ServiceOrderStatus.class);
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(serviceOrderRepository).findByStatus(statusCaptor.capture(), pageableCaptor.capture());
            assertThat(statusCaptor.getValue()).isEqualTo(ServiceOrderStatus.RECEIVED);
            assertThat(pageableCaptor.getValue()).isEqualTo(pageable);
        }
    }

    @Nested
    class ListFinishedServiceOrders {

        @Test
        void shouldReturnMappedPageOfFinishedServiceOrders() {
            var pageable = PageRequest.of(0, 10);
            var serviceOrder = finishedServiceOrder();
            Page<ServiceOrder> page = new PageImpl<>(List.of(serviceOrder), pageable, 1);
            when(serviceOrderRepository.findByStatus(ServiceOrderStatus.FINISHED, pageable)).thenReturn(page);

            var result = serviceOrderService.listFinished(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).status()).isEqualTo("FINISHED");
            assertThat(result.getContent().get(0).vehicleId()).isEqualTo(VALID_VEHICLE_ID);
            assertThat(result.getContent().get(0).customerId()).isEqualTo(VALID_CUSTOMER_ID);
        }

        @Test
        void shouldReturnEmptyPageWhenNoFinishedServiceOrders() {
            var pageable = PageRequest.of(0, 10);
            when(serviceOrderRepository.findByStatus(ServiceOrderStatus.FINISHED, pageable))
                    .thenReturn(Page.empty(pageable));

            var result = serviceOrderService.listFinished(pageable);

            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        void shouldQueryRepositoryWithFinishedStatusAndProvidedPageable() {
            var pageable = PageRequest.of(2, 5);
            when(serviceOrderRepository.findByStatus(any(), any())).thenReturn(Page.empty(pageable));

            serviceOrderService.listFinished(pageable);

            var statusCaptor = ArgumentCaptor.forClass(ServiceOrderStatus.class);
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(serviceOrderRepository).findByStatus(statusCaptor.capture(), pageableCaptor.capture());
            assertThat(statusCaptor.getValue()).isEqualTo(ServiceOrderStatus.FINISHED);
            assertThat(pageableCaptor.getValue()).isEqualTo(pageable);
        }
    }

    @Nested
    class StartDiagnosticServiceOrder {

        @Test
        void shouldStartDiagnosticSuccessfully() {
            var serviceOrder = receivedServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            var result = serviceOrderService.startDiagnostic(VALID_SERVICE_ORDER_ID);

            assertThat(result.status()).isEqualTo("IN_DIAGNOSTIC");
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceOrderService.startDiagnostic(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }

        @Test
        void shouldThrowInvalidTransitionWhenOrderIsNotReceived() {
            var serviceOrder = cancelledServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            assertThatThrownBy(() -> serviceOrderService.startDiagnostic(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);
        }
    }

    @Nested
    class GenerateQuote {

        @Test
        void shouldGenerateQuoteSuccessfully() {
            var serviceOrder = inDiagnosticServiceOrder();
            var request = quoteRequest();
            var quoteId = UUID.randomUUID();
            var quote = quoteResponse(quoteId);

            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(quoteService.create(VALID_SERVICE_ORDER_ID, request)).thenReturn(quote);
            when(customerService.identifyCustomerById(VALID_CUSTOMER_ID)).thenReturn(customerResponse());

            var result = serviceOrderService.generateQuote(VALID_SERVICE_ORDER_ID, request);

            assertThat(result).isEqualTo(quote);
            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
            verify(notificationService).createQuoteNotification(quoteId, "João Silva", new BigDecimal("1500.00"));
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceOrderService.generateQuote(VALID_SERVICE_ORDER_ID, quoteRequest()))
                    .isInstanceOf(ServiceOrderNotFoundException.class);

            verify(quoteService, never()).create(any(), any());
            verify(customerService, never()).identifyCustomerById(any());
            verify(notificationService, never()).createQuoteNotification(any(), any(), any());
        }

        @Test
        void shouldThrowInvalidTransitionWhenOrderIsNotInDiagnostic() {
            var serviceOrder = receivedServiceOrder();
            var request = quoteRequest();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(quoteService.create(VALID_SERVICE_ORDER_ID, request)).thenReturn(quoteResponse(UUID.randomUUID()));

            assertThatThrownBy(() -> serviceOrderService.generateQuote(VALID_SERVICE_ORDER_ID, request))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);

            verify(customerService, never()).identifyCustomerById(any());
            verify(notificationService, never()).createQuoteNotification(any(), any(), any());
        }
    }

    @Nested
    class DeliverToCustomerServiceOrder {

        @Test
        void shouldDeliverServiceOrderSuccessfully() {
            var serviceOrder = finishedServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            var result = serviceOrderService.deliverToCustomer(VALID_SERVICE_ORDER_ID);

            assertThat(result.status()).isEqualTo("DELIVERED");
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceOrderService.deliverToCustomer(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }

        @Test
        void shouldThrowInvalidTransitionWhenOrderIsNotFinished() {
            var serviceOrder = cancelledServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            assertThatThrownBy(() -> serviceOrderService.deliverToCustomer(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);
        }
    }

    @Nested
    class CloseServiceOrder {

        @Test
        void shouldCancelServiceOrderSuccessfully() {
            var serviceOrder = serviceOrderAwaitingApproval();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            var result = serviceOrderService.close(VALID_SERVICE_ORDER_ID);

            assertThat(result.status()).isEqualTo("CANCELLED");
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceOrderService.close(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }

        @Test
        void shouldThrowInvalidTransitionWhenOrderIsAlreadyCancelled() {
            var serviceOrder = cancelledServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            assertThatThrownBy(() -> serviceOrderService.close(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);
        }
    }

    @Nested
    class GetStatusServiceOrder {

        @Test
        void shouldReturnStatusResponseSuccessfully() {
            var serviceOrder = receivedServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            var result = serviceOrderService.getStatus(VALID_SERVICE_ORDER_ID);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(serviceOrder.getId());
            assertThat(result.vehicleId()).isEqualTo(VALID_VEHICLE_ID);
            assertThat(result.customerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(result.status()).isEqualTo(ServiceOrderStatus.RECEIVED);
        }

        @Test
        void shouldReturnCurrentStatusForFinishedOrder() {
            var serviceOrder = finishedServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            var result = serviceOrderService.getStatus(VALID_SERVICE_ORDER_ID);

            assertThat(result.status()).isEqualTo(ServiceOrderStatus.FINISHED);
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceOrderService.getStatus(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }
    }

    private ServiceOrderRequestDTO validRequest() {
        return new ServiceOrderRequestDTO(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT);
    }

    private VehicleResponseDTO vehicleOwnedByCustomer() {
        return new VehicleResponseDTO(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "ABC1234",
                "Civic", "Honda", "Preto", 2022, null, LocalDateTime.now(), LocalDateTime.now());
    }

    private VehicleResponseDTO vehicleOwnedByOther() {
        return new VehicleResponseDTO(VALID_VEHICLE_ID, UUID.randomUUID(), "ABC1234",
                "Civic", "Honda", "Preto", 2022, null, LocalDateTime.now(), LocalDateTime.now());
    }

    private ServiceOrder receivedServiceOrder() {
        return ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_USER_ID);
    }

    private ServiceOrder finishedServiceOrder() {
        var order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_USER_ID);
        order.startDiagnostic();
        order.sendToApproval();
        order.approve();
        order.startExecution();
        order.finish();
        return order;
    }

    private ServiceOrder serviceOrderAwaitingApproval() {
        var order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_USER_ID);
        order.startDiagnostic();
        order.sendToApproval();
        return order;
    }

    private ServiceOrder inDiagnosticServiceOrder() {
        var order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_USER_ID);
        order.startDiagnostic();
        return order;
    }

    private CustomerResponseDTO customerResponse() {
        return new CustomerResponseDTO(VALID_CUSTOMER_ID, "12345678900", "João Silva",
                "joao@example.com", "11999990000", LocalDateTime.now(), LocalDateTime.now());
    }

    private CreateQuoteRequestDTO quoteRequest() {
        return new CreateQuoteRequestDTO(VALID_SERVICE_ORDER_ID, List.of(), List.of());
    }

    private QuoteResponseDTO quoteResponse(UUID quoteId) {
        return new QuoteResponseDTO(quoteId, VALID_SERVICE_ORDER_ID, QuoteStatus.PENDING,
                new BigDecimal("1500.00"), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private ServiceOrder cancelledServiceOrder() {
        var order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_USER_ID);
        order.cancel();
        return order;
    }
}
