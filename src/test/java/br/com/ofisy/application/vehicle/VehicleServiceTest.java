package br.com.ofisy.application.vehicle;

import br.com.ofisy.application.vehicle.dto.VehicleRequestDTO;
import br.com.ofisy.application.vehicle.exceptions.VehicleAlreadyExistsException;
import br.com.ofisy.application.vehicle.exceptions.VehicleLicensePlateNotFoundException;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import br.com.ofisy.domain.vehicle.exceptions.InvalidLicensePlateException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final String VALID_OLD_PLATE = "ABC1234";
    private static final String VALID_MERCOSUL_PLATE = "ABC1D23";
    private static final String VALID_MODEL = "Civic";
    private static final String VALID_BRAND = "Honda";
    private static final String VALID_COLOR = "Preto";
    private static final Integer VALID_YEAR = 2022;
    private static final String VALID_DESCRIPTION = "Barulho na suspensão";

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleRequestDTO validRequest() {
        return new VehicleRequestDTO(VALID_CUSTOMER_ID, VALID_OLD_PLATE, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);
    }

    private Vehicle validVehicle() {
        return Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(VALID_OLD_PLATE), VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);
    }

    @Nested
    class RegisterVehicle {

        @ParameterizedTest
        @ValueSource(strings = {VALID_OLD_PLATE, VALID_MERCOSUL_PLATE})
        void shouldSaveVehicleWithValidPlate(String plate) {
            var request = new VehicleRequestDTO(VALID_CUSTOMER_ID, plate, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);
            when(vehicleRepository.findByLicensePlate(any(LicensePlate.class))).thenReturn(Optional.empty());
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

            var response = vehicleService.registerVehicle(request);

            var captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(vehicleRepository).save(captor.capture());
            var saved = captor.getValue();
            assertThat(saved.getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(saved.getLicensePlate().getValue()).isEqualTo(plate);
            assertThat(saved.getModel()).isEqualTo(VALID_MODEL);
            assertThat(saved.getBrand()).isEqualTo(VALID_BRAND);
            assertThat(saved.getColor()).isEqualTo(VALID_COLOR);
            assertThat(saved.getYear()).isEqualTo(VALID_YEAR);

            assertThat(response).isNotNull();
            assertThat(response.customerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(response.licensePlate()).isEqualTo(plate);
            assertThat(response.model()).isEqualTo(VALID_MODEL);
            assertThat(response.brand()).isEqualTo(VALID_BRAND);
            assertThat(response.color()).isEqualTo(VALID_COLOR);
            assertThat(response.year()).isEqualTo(VALID_YEAR);
            assertThat(response.createdAt()).isNotNull();
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        void shouldCallRepositorySaveExactlyOnce() {
            when(vehicleRepository.findByLicensePlate(any(LicensePlate.class))).thenReturn(Optional.empty());
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

            vehicleService.registerVehicle(validRequest());

            verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {VALID_OLD_PLATE, VALID_MERCOSUL_PLATE})
        void shouldThrowVehicleAlreadyExistsExceptionWhenLicensePlateAlreadyRegistered(String plate) {
            var request = new VehicleRequestDTO(VALID_CUSTOMER_ID, plate, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);
            when(vehicleRepository.findByLicensePlate(any(LicensePlate.class))).thenReturn(Optional.of(validVehicle()));

            assertThatThrownBy(() -> vehicleService.registerVehicle(request))
                    .isInstanceOf(VehicleAlreadyExistsException.class)
                    .hasMessageContaining(plate);

            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }
    }

    @Nested
    class ListRegisteredVehicles {

        @Test
        void shouldReturnPageOfVehicleDTOs() {
            var vehicle = validVehicle();
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(vehicle), pageable, 1);
            when(vehicleRepository.findAll(pageable)).thenReturn(page);

            var result = vehicleService.listRegisteredVehicles(pageable);

            assertThat(result.getContent()).hasSize(1);
            var dto = result.getContent().getFirst();
            assertThat(dto.customerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(dto.licensePlate()).isEqualTo(VALID_OLD_PLATE);
            assertThat(dto.model()).isEqualTo(VALID_MODEL);
            assertThat(dto.brand()).isEqualTo(VALID_BRAND);
            assertThat(dto.color()).isEqualTo(VALID_COLOR);
            assertThat(dto.year()).isEqualTo(VALID_YEAR);
            assertThat(dto.createdAt()).isNotNull();
            assertThat(dto.updatedAt()).isNotNull();
        }

        @Test
        void shouldReturnEmptyPageWhenNoVehicles() {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<Vehicle>(Collections.emptyList(), pageable, 0);
            when(vehicleRepository.findAll(pageable)).thenReturn(emptyPage);

            var result = vehicleService.listRegisteredVehicles(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        void shouldReturnMultipleVehicles() {
            var vehicle1 = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate("ABC1234"), "Model A", "Brand A", "Branco", 2020, null);
            var vehicle2 = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate("XYZ9999"), "Model B", "Brand B", "Preto", 2021, null);
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(vehicle1, vehicle2), pageable, 2);
            when(vehicleRepository.findAll(pageable)).thenReturn(page);

            var result = vehicleService.listRegisteredVehicles(pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).licensePlate()).isEqualTo("ABC1234");
            assertThat(result.getContent().get(1).licensePlate()).isEqualTo("XYZ9999");
        }
    }

    @Nested
    class ListVehiclesFromCustomer {

        @Test
        void shouldReturnListOfVehicleDTOsForCustomer() {
            var vehicle = validVehicle();
            when(vehicleRepository.findByCustomerId(VALID_CUSTOMER_ID)).thenReturn(List.of(vehicle));

            var result = vehicleService.listVehiclesFromCustomer(VALID_CUSTOMER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().customerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(result.getFirst().licensePlate()).isEqualTo(VALID_OLD_PLATE);
        }

        @Test
        void shouldReturnEmptyListWhenCustomerHasNoVehicles() {
            when(vehicleRepository.findByCustomerId(VALID_CUSTOMER_ID)).thenReturn(Collections.emptyList());

            var result = vehicleService.listVehiclesFromCustomer(VALID_CUSTOMER_ID);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnMultipleVehiclesForCustomer() {
            var vehicle1 = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate("ABC1234"), "Model A", "Brand A", "Branco", 2020, null);
            var vehicle2 = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate("XYZ9999"), "Model B", "Brand B", "Preto", 2021, null);
            when(vehicleRepository.findByCustomerId(VALID_CUSTOMER_ID)).thenReturn(List.of(vehicle1, vehicle2));

            var result = vehicleService.listVehiclesFromCustomer(VALID_CUSTOMER_ID);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    class IdentifyVehicleById {

        @Test
        void shouldReturnVehicleDTOWhenFound() {
            var id = UUID.randomUUID();
            var vehicle = validVehicle();
            when(vehicleRepository.findById(id)).thenReturn(Optional.of(vehicle));

            var result = vehicleService.identifyVehicleById(id);

            assertThat(result).isNotNull();
            assertThat(result.customerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(result.licensePlate()).isEqualTo(VALID_OLD_PLATE);
            assertThat(result.model()).isEqualTo(VALID_MODEL);
        }

        @Test
        void shouldThrowVehicleNotFoundExceptionWhenNotFound() {
            var id = UUID.randomUUID();
            when(vehicleRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.identifyVehicleById(id))
                    .isInstanceOf(VehicleNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }

    @Nested
    class IdentifyVehicleByLicensePlate {

        @Test
        void shouldReturnVehicleDTOWhenFound() {
            var vehicle = validVehicle();
            when(vehicleRepository.findByLicensePlate(any(LicensePlate.class))).thenReturn(Optional.of(vehicle));

            var result = vehicleService.identifyVehicleByLicensePlate(VALID_OLD_PLATE);

            assertThat(result).isNotNull();
            assertThat(result.licensePlate()).isEqualTo(VALID_OLD_PLATE);
            assertThat(result.customerId()).isEqualTo(VALID_CUSTOMER_ID);
        }

        @Test
        void shouldThrowVehicleLicensePlateNotFoundExceptionWhenNotFound() {
            when(vehicleRepository.findByLicensePlate(any(LicensePlate.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.identifyVehicleByLicensePlate(VALID_OLD_PLATE))
                    .isInstanceOf(VehicleLicensePlateNotFoundException.class)
                    .hasMessageContaining(VALID_OLD_PLATE);
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "12345", "AB1234", ""})
        void shouldThrowInvalidLicensePlateExceptionForInvalidFormat(String invalidPlate) {
            assertThatThrownBy(() -> vehicleService.identifyVehicleByLicensePlate(invalidPlate))
                    .isInstanceOf(InvalidLicensePlateException.class);
        }

        @Test
        void shouldThrowNullPointerExceptionForNullLicensePlate() {
            assertThatThrownBy(() -> vehicleService.identifyVehicleByLicensePlate(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
