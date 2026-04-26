package br.com.ofisy.application.serviceorder;

import br.com.ofisy.application.customer.CustomerService;
import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.user.UserService;
import br.com.ofisy.application.user.exceptions.EmailNotFoundException;
import br.com.ofisy.application.vehicle.VehicleService;
import br.com.ofisy.application.vehicle.dto.VehicleResponseDTO;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
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

    @InjectMocks
    private ServiceOrderService serviceOrderService;

    @Nested
    class CreateServiceOrder {

        @Test
        void shouldCreateServiceOrderSuccessfully() {
            when(vehicleService.identifyVehicleById(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByCustomer());
            when(userService.getIdByEmail(VALID_EMAIL)).thenReturn(VALID_USER_ID);
            when(serviceOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var result = serviceOrderService.createServiceOrder(validRequest(), VALID_EMAIL);

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

            var result = serviceOrderService.createServiceOrder(request, VALID_EMAIL);

            assertThat(result.report()).isNull();
            verify(serviceOrderRepository).save(any());
        }

        @Test
        void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
            doThrow(new CustomerNotFoundException(VALID_CUSTOMER_ID))
                    .when(customerService).identifyCustomerById(VALID_CUSTOMER_ID);

            var request = validRequest();
            assertThatThrownBy(() -> serviceOrderService.createServiceOrder(request, VALID_EMAIL))
                    .isInstanceOf(CustomerNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }

        @Test
        void shouldThrowVehicleNotFoundExceptionWhenVehicleDoesNotExist() {
            doThrow(new VehicleNotFoundException(VALID_VEHICLE_ID))
                    .when(vehicleService).identifyVehicleById(VALID_VEHICLE_ID);

            var request = validRequest();
            assertThatThrownBy(() -> serviceOrderService.createServiceOrder(request, VALID_EMAIL))
                    .isInstanceOf(VehicleNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }

        @Test
        void shouldThrowVehicleNotOwnedByCustomerExceptionWhenOwnershipMismatch() {
            when(vehicleService.identifyVehicleById(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByOther());

            var request = validRequest();
            assertThatThrownBy(() -> serviceOrderService.createServiceOrder(request, VALID_EMAIL))
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
            assertThatThrownBy(() -> serviceOrderService.createServiceOrder(request, VALID_EMAIL))
                    .isInstanceOf(EmailNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
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
}
