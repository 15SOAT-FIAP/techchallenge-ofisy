package br.com.ofisy.application.serviceorder.create;

import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.customer.identifybyid.IdentifyByIdCustomerUseCase;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.user.exceptions.EmailNotFoundException;
import br.com.ofisy.application.user.getidbyemail.GetIdByEmailUseCase;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.application.vehicle.identifybyid.IdentifyVehicleByIdUseCase;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateServiceOrderServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final String VALID_EMAIL = "mecanico@ofisy.com";
    private static final String VALID_REPORT = "Barulho na suspensão dianteira";

    @Mock
    private ServiceOrderRepository serviceOrderRepository;
    @Mock
    private IdentifyByIdCustomerUseCase identifyByIdCustomerUseCase;
    @Mock
    private IdentifyVehicleByIdUseCase identifyVehicleByIdUseCase;
    @Mock
    private GetIdByEmailUseCase getIdByEmailUseCase;

    @InjectMocks
    private CreateServiceOrderService createServiceOrderService;

    private CreateServiceOrderUseCase.CreateServiceOrderCommand validCommand() {
        return new CreateServiceOrderUseCase.CreateServiceOrderCommand(
                VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_EMAIL);
    }

    @Nested
    class Execute {

        @Test
        void shouldCreateServiceOrderSuccessfully() {
            when(identifyVehicleByIdUseCase.execute(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByCustomer());
            when(getIdByEmailUseCase.execute(VALID_EMAIL)).thenReturn(VALID_USER_ID);
            when(serviceOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ServiceOrder result = createServiceOrderService.execute(validCommand());

            assertThat(result).isNotNull();
            assertThat(result.getVehicleId()).isEqualTo(VALID_VEHICLE_ID);
            assertThat(result.getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(result.getReport()).isEqualTo(VALID_REPORT);
            assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
            verify(serviceOrderRepository).save(any());
        }

        @Test
        void shouldCreateServiceOrderWithNullReport() {
            CreateServiceOrderUseCase.CreateServiceOrderCommand cmd =
                    new CreateServiceOrderUseCase.CreateServiceOrderCommand(
                            VALID_VEHICLE_ID, VALID_CUSTOMER_ID, null, VALID_EMAIL);

            when(identifyVehicleByIdUseCase.execute(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByCustomer());
            when(getIdByEmailUseCase.execute(VALID_EMAIL)).thenReturn(VALID_USER_ID);
            when(serviceOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ServiceOrder result = createServiceOrderService.execute(cmd);

            assertThat(result.getReport()).isNull();
            verify(serviceOrderRepository).save(any());
        }

        @Test
        void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
            doThrow(new CustomerNotFoundException(VALID_CUSTOMER_ID))
                    .when(identifyByIdCustomerUseCase).execute(VALID_CUSTOMER_ID);

            assertThatThrownBy(() -> createServiceOrderService.execute(validCommand()))
                    .isInstanceOf(CustomerNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }

        @Test
        void shouldThrowVehicleNotFoundExceptionWhenVehicleDoesNotExist() {
            doThrow(new VehicleNotFoundException(VALID_VEHICLE_ID))
                    .when(identifyVehicleByIdUseCase).execute(VALID_VEHICLE_ID);

            assertThatThrownBy(() -> createServiceOrderService.execute(validCommand()))
                    .isInstanceOf(VehicleNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }

        @Test
        void shouldThrowVehicleNotOwnedByCustomerExceptionWhenOwnershipMismatch() {
            when(identifyVehicleByIdUseCase.execute(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByOther());

            assertThatThrownBy(() -> createServiceOrderService.execute(validCommand()))
                    .isInstanceOf(VehicleNotOwnedByCustomerException.class)
                    .hasMessageContaining(VALID_VEHICLE_ID.toString())
                    .hasMessageContaining(VALID_CUSTOMER_ID.toString());

            verify(serviceOrderRepository, never()).save(any());
        }

        @Test
        void shouldThrowEmailNotFoundExceptionWhenUserDoesNotExist() {
            when(identifyVehicleByIdUseCase.execute(VALID_VEHICLE_ID)).thenReturn(vehicleOwnedByCustomer());
            doThrow(new EmailNotFoundException(VALID_EMAIL))
                    .when(getIdByEmailUseCase).execute(VALID_EMAIL);

            assertThatThrownBy(() -> createServiceOrderService.execute(validCommand()))
                    .isInstanceOf(EmailNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }
    }

    private Vehicle vehicleOwnedByCustomer() {
        return Vehicle.reconstruct(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, new LicensePlate("ABC1234"),
                "Civic", "Honda", "Preto", 2022, null, LocalDateTime.now(), LocalDateTime.now());
    }

    private Vehicle vehicleOwnedByOther() {
        return Vehicle.reconstruct(VALID_VEHICLE_ID, UUID.randomUUID(), new LicensePlate("ABC1234"),
                "Civic", "Honda", "Preto", 2022, null, LocalDateTime.now(), LocalDateTime.now());
    }
}