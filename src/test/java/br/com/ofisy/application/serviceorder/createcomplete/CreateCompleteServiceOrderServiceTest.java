package br.com.ofisy.application.serviceorder.createcomplete;

import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.customer.identifybyid.IdentifyByIdCustomerUseCase;
import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.user.exceptions.EmailNotFoundException;
import br.com.ofisy.application.user.getidbyemail.GetIdByEmailUseCase;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.application.vehicle.identifybyid.IdentifyVehicleByIdUseCase;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCompleteServiceOrderServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final UUID VALID_STOCK_ID = UUID.randomUUID();
    private static final UUID VALID_SERVICE_CATALOG_ID = UUID.randomUUID();
    private static final String VALID_EMAIL = "mecanico@ofisy.com";
    private static final String VALID_REPORT = "Barulho na suspensão dianteira";

    @Mock private ServiceOrderRepository serviceOrderRepository;
    @Mock private IdentifyByIdCustomerUseCase identifyByIdCustomerUseCase;
    @Mock private IdentifyVehicleByIdUseCase identifyVehicleByIdUseCase;
    @Mock private GetIdByEmailUseCase getIdByEmailUseCase;
    @Mock private CreateQuoteUseCase createQuoteUseCase;

    @InjectMocks
    private CreateCompleteServiceOrderService service;

    @Nested
    class Execute {

        @Test
        void shouldCreateCompleteServiceOrderWithStockItemsSuccessfully() {
            List<CreateQuoteUseCase.StockItemCommand> stockItems = List.of(new CreateQuoteUseCase.StockItemCommand(VALID_STOCK_ID, 2));
            CreateCompleteServiceOrderUseCase.CreateCompleteServiceOrderCommand cmd = validCommand(stockItems, List.of());
            ServiceOrder savedOrder = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_USER_ID);

            when(identifyVehicleByIdUseCase.execute(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByCustomer());
            when(getIdByEmailUseCase.execute(VALID_EMAIL)).thenReturn(VALID_USER_ID);
            when(serviceOrderRepository.save(any())).thenReturn(savedOrder);
            when(createQuoteUseCase.execute(any())).thenReturn(pendingQuote(savedOrder.getId()));

            ServiceOrder result = service.execute(cmd);

            assertThat(result).isNotNull();
            assertThat(result.getVehicleId()).isEqualTo(VALID_VEHICLE_ID);
            assertThat(result.getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
            verify(createQuoteUseCase).execute(any());
        }

        @Test
        void shouldCreateCompleteServiceOrderWithServiceItemsSuccessfully() {
            List<CreateQuoteUseCase.ServiceItemCommand> serviceItems = List.of(new CreateQuoteUseCase.ServiceItemCommand(VALID_SERVICE_CATALOG_ID));
            CreateCompleteServiceOrderUseCase.CreateCompleteServiceOrderCommand cmd = validCommand(List.of(), serviceItems);
            ServiceOrder savedOrder = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_USER_ID);

            when(identifyVehicleByIdUseCase.execute(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByCustomer());
            when(getIdByEmailUseCase.execute(VALID_EMAIL)).thenReturn(VALID_USER_ID);
            when(serviceOrderRepository.save(any())).thenReturn(savedOrder);
            when(createQuoteUseCase.execute(any())).thenReturn(pendingQuote(savedOrder.getId()));

            ServiceOrder result = service.execute(cmd);

            assertThat(result).isNotNull();
            verify(createQuoteUseCase).execute(any());
        }

        @Test
        void shouldTransitionThroughAllStatesWhenItemsProvided() {
            List<CreateQuoteUseCase.StockItemCommand> stockItems = List.of(new CreateQuoteUseCase.StockItemCommand(VALID_STOCK_ID, 1));
            CreateCompleteServiceOrderUseCase.CreateCompleteServiceOrderCommand cmd = validCommand(stockItems, List.of());
            ServiceOrder savedOrder = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_USER_ID);

            when(identifyVehicleByIdUseCase.execute(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByCustomer());
            when(getIdByEmailUseCase.execute(VALID_EMAIL)).thenReturn(VALID_USER_ID);
            when(serviceOrderRepository.save(any())).thenReturn(savedOrder);
            when(createQuoteUseCase.execute(any())).thenReturn(pendingQuote(savedOrder.getId()));

            service.execute(cmd);

            assertThat(savedOrder.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        }

        @Test
        void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
            doThrow(new CustomerNotFoundException(VALID_CUSTOMER_ID))
                    .when(identifyByIdCustomerUseCase).execute(VALID_CUSTOMER_ID);

            assertThatThrownBy(() -> service.execute(validCommand(List.of(), List.of())))
                    .isInstanceOf(CustomerNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
            verify(createQuoteUseCase, never()).execute(any());
        }

        @Test
        void shouldThrowVehicleNotFoundExceptionWhenVehicleDoesNotExist() {
            doThrow(new VehicleNotFoundException(VALID_VEHICLE_ID))
                    .when(identifyVehicleByIdUseCase).execute(VALID_VEHICLE_ID);

            assertThatThrownBy(() -> service.execute(validCommand(List.of(), List.of())))
                    .isInstanceOf(VehicleNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
            verify(createQuoteUseCase, never()).execute(any());
        }

        @Test
        void shouldThrowVehicleNotOwnedByCustomerWhenOwnershipMismatch() {
            when(identifyVehicleByIdUseCase.execute(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByOther());

            assertThatThrownBy(() -> service.execute(validCommand(List.of(), List.of())))
                    .isInstanceOf(VehicleNotOwnedByCustomerException.class);

            verify(serviceOrderRepository, never()).save(any());
            verify(createQuoteUseCase, never()).execute(any());
        }

        @Test
        void shouldThrowEmailNotFoundExceptionWhenUserDoesNotExist() {
            when(identifyVehicleByIdUseCase.execute(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByCustomer());
            doThrow(new EmailNotFoundException(VALID_EMAIL))
                    .when(getIdByEmailUseCase).execute(VALID_EMAIL);

            assertThatThrownBy(() -> service.execute(validCommand(List.of(), List.of())))
                    .isInstanceOf(EmailNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
            verify(createQuoteUseCase, never()).execute(any());
        }
    }

    private CreateCompleteServiceOrderUseCase.CreateCompleteServiceOrderCommand validCommand(
            List<CreateQuoteUseCase.StockItemCommand> stockItems,
            List<CreateQuoteUseCase.ServiceItemCommand> serviceItems) {
        return new CreateCompleteServiceOrderUseCase.CreateCompleteServiceOrderCommand(
                VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_EMAIL,
                stockItems, serviceItems);
    }

    private Vehicle vehicleOwnedByCustomer() {
        return Vehicle.reconstruct(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, new LicensePlate("ABC1234"),
                "Civic", "Honda", "Preto", 2022, null, LocalDateTime.now(), LocalDateTime.now());
    }

    private Vehicle vehicleOwnedByOther() {
        return Vehicle.reconstruct(VALID_VEHICLE_ID, UUID.randomUUID(), new LicensePlate("ABC1234"),
                "Civic", "Honda", "Preto", 2022, null, LocalDateTime.now(), LocalDateTime.now());
    }

    private Quote pendingQuote(UUID serviceOrderId) {
        return Quote.reconstruct(UUID.randomUUID(), serviceOrderId, QuoteStatus.PENDING,
                new BigDecimal("100.00"), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }
}
