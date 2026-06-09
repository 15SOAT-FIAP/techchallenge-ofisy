package br.com.ofisy.application.vehicle.listbycustomer;

import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListVehiclesByCustomerServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private ListVehiclesByCustomerService listVehiclesByCustomerService;

    @Nested
    class ListVehiclesByCustomer {

        @Test
        void shouldReturnVehiclesForCustomer() {
            Vehicle vehicle = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate("ABC1234"),
                    "Civic", "Honda", "Preto", 2022, null);
            when(vehicleRepository.findByCustomerId(VALID_CUSTOMER_ID)).thenReturn(List.of(vehicle));

            List<Vehicle> result = listVehiclesByCustomerService.execute(VALID_CUSTOMER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
            verify(vehicleRepository).findByCustomerId(VALID_CUSTOMER_ID);
        }

        @Test
        void shouldReturnEmptyListWhenNoVehiclesForCustomer() {
            when(vehicleRepository.findByCustomerId(VALID_CUSTOMER_ID)).thenReturn(List.of());

            List<Vehicle> result = listVehiclesByCustomerService.execute(VALID_CUSTOMER_ID);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldDelegateToRepository() {
            when(vehicleRepository.findByCustomerId(VALID_CUSTOMER_ID)).thenReturn(List.of());

            listVehiclesByCustomerService.execute(VALID_CUSTOMER_ID);

            verify(vehicleRepository).findByCustomerId(VALID_CUSTOMER_ID);
        }
    }
}