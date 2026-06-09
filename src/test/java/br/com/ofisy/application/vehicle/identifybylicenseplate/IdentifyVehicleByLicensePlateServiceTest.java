package br.com.ofisy.application.vehicle.identifybylicenseplate;

import br.com.ofisy.application.vehicle.exceptions.VehicleLicensePlateNotFoundException;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentifyVehicleByLicensePlateServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final String VALID_OLD_PLATE = "ABC1234";
    private static final String VALID_MERCOSUL_PLATE = "ABC1D23";

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private IdentifyVehicleByLicensePlateService identifyVehicleByLicensePlateService;

    @Nested
    class IdentifyVehicleByLicensePlate {

        @ParameterizedTest
        @ValueSource(strings = {VALID_OLD_PLATE, VALID_MERCOSUL_PLATE})
        void shouldReturnVehicleWhenPlateExists(String plate) {
            Vehicle vehicle = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(plate),
                    "Civic", "Honda", "Preto", 2022, null);
            when(vehicleRepository.findByLicensePlate(any(LicensePlate.class))).thenReturn(Optional.of(vehicle));

            Vehicle result = identifyVehicleByLicensePlateService.execute(plate);

            assertThat(result).isNotNull();
            assertThat(result.getLicensePlate().getValue()).isEqualTo(plate);
            assertThat(result.getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
        }

        @Test
        void shouldThrowVehicleLicensePlateNotFoundExceptionWhenPlateDoesNotExist() {
            when(vehicleRepository.findByLicensePlate(any(LicensePlate.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> identifyVehicleByLicensePlateService.execute(VALID_OLD_PLATE))
                    .isInstanceOf(VehicleLicensePlateNotFoundException.class)
                    .hasMessageContaining(VALID_OLD_PLATE);
        }
    }
}