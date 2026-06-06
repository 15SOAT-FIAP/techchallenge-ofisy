package br.com.ofisy.application.vehicle.identifybyid;

import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentifyVehicleByIdServiceTest {

    private static final UUID VALID_ID = UUID.randomUUID();
    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private IdentifyVehicleByIdService identifyVehicleByIdService;

    @Nested
    class IdentifyVehicleById {

        @Test
        void shouldReturnVehicleWhenIdExists() {
            Vehicle vehicle = validVehicle();
            when(vehicleRepository.findById(VALID_ID)).thenReturn(Optional.of(vehicle));

            Vehicle result = identifyVehicleByIdService.execute(VALID_ID);

            assertThat(result).isNotNull();
            assertThat(result.getLicensePlate().getValue()).isEqualTo("ABC1234");
            assertThat(result.getModel()).isEqualTo("Civic");
            assertThat(result.getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
        }

        @Test
        void shouldThrowVehicleNotFoundExceptionWhenIdDoesNotExist() {
            when(vehicleRepository.findById(VALID_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> identifyVehicleByIdService.execute(VALID_ID))
                    .isInstanceOf(VehicleNotFoundException.class)
                    .hasMessageContaining(VALID_ID.toString());
        }
    }

    private Vehicle validVehicle() {
        return Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate("ABC1234"),
                "Civic", "Honda", "Preto", 2022, null);
    }
}