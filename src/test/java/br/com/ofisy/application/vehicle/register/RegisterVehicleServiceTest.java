package br.com.ofisy.application.vehicle.register;

import br.com.ofisy.application.vehicle.exceptions.VehicleAlreadyExistsException;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterVehicleServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final String VALID_OLD_PLATE = "ABC1234";
    private static final String VALID_MERCOSUL_PLATE = "ABC1D23";
    private static final String VALID_MODEL = "Civic";
    private static final String VALID_BRAND = "Honda";
    private static final String VALID_COLOR = "Preto";
    private static final Integer VALID_YEAR = 2022;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private RegisterVehicleService registerVehicleService;

    @Nested
    class RegisterVehicle {

        @ParameterizedTest
        @ValueSource(strings = {VALID_OLD_PLATE, VALID_MERCOSUL_PLATE})
        void shouldSaveAndReturnVehicleWhenPlateIsNew(String plate) {
            RegisterVehicleUseCase.RegisterVehicleCommand cmd = validCommand(plate);
            when(vehicleRepository.findByLicensePlate(any(LicensePlate.class))).thenReturn(Optional.empty());
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

            Vehicle result = registerVehicleService.execute(cmd);

            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepository).save(captor.capture());
            Vehicle saved = captor.getValue();
            assertThat(saved.getLicensePlate().getValue()).isEqualTo(plate);
            assertThat(saved.getModel()).isEqualTo(VALID_MODEL);
            assertThat(saved.getBrand()).isEqualTo(VALID_BRAND);
            assertThat(saved.getColor()).isEqualTo(VALID_COLOR);
            assertThat(saved.getYear()).isEqualTo(VALID_YEAR);
            assertThat(saved.getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getLicensePlate().getValue()).isEqualTo(plate);
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();
        }

        @Test
        void shouldCallRepositorySaveExactlyOnce() {
            when(vehicleRepository.findByLicensePlate(any(LicensePlate.class))).thenReturn(Optional.empty());
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

            registerVehicleService.execute(validCommand(VALID_OLD_PLATE));

            verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {VALID_OLD_PLATE, VALID_MERCOSUL_PLATE})
        void shouldThrowVehicleAlreadyExistsExceptionWhenPlateAlreadyRegistered(String plate) {
            Vehicle existing = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(plate),
                    VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, null);
            when(vehicleRepository.findByLicensePlate(any(LicensePlate.class))).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> registerVehicleService.execute(validCommand(plate)))
                    .isInstanceOf(VehicleAlreadyExistsException.class)
                    .hasMessageContaining(plate);

            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }
    }

    private RegisterVehicleUseCase.RegisterVehicleCommand validCommand(String plate) {
        return new RegisterVehicleUseCase.RegisterVehicleCommand(
                VALID_CUSTOMER_ID, plate, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, null);
    }
}