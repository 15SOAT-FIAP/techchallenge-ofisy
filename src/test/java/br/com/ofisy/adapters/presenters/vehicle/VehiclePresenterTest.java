package br.com.ofisy.adapters.presenters.vehicle;

import br.com.ofisy.adapters.controllers.vehicle.dto.VehicleResponseDTO;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehiclePresenterTest {

    private static final String VALID_OLD_PLATE = "ABC1234";
    private static final String VALID_MERCOSUL_PLATE = "ABC1D23";
    private static final UUID VALID_ID = UUID.randomUUID();
    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final String VALID_MODEL = "Civic";
    private static final String VALID_BRAND = "Honda";
    private static final String VALID_COLOR = "Preto";
    private static final Integer VALID_YEAR = 2022;
    private static final String VALID_DESCRIPTION = "Barulho na suspensao";

    @Nested
    class Present {

        @ParameterizedTest
        @ValueSource(strings = {VALID_OLD_PLATE, VALID_MERCOSUL_PLATE})
        void shouldMapAllFieldsCorrectly(String plate) {
            Vehicle vehicle = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(plate),
                    VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

            VehicleResponseDTO dto = VehiclePresenter.present(vehicle);

            assertThat(dto).isNotNull();
            assertThat(dto.customerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(dto.licensePlate()).isEqualTo(plate);
            assertThat(dto.model()).isEqualTo(VALID_MODEL);
            assertThat(dto.brand()).isEqualTo(VALID_BRAND);
            assertThat(dto.color()).isEqualTo(VALID_COLOR);
            assertThat(dto.year()).isEqualTo(VALID_YEAR);
            assertThat(dto.description()).isEqualTo(VALID_DESCRIPTION);
        }

        @Test
        void shouldSetTimestampsFromCreatedVehicle() {
            Vehicle vehicle = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(VALID_OLD_PLATE),
                    VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, null);

            VehicleResponseDTO dto = VehiclePresenter.present(vehicle);

            assertThat(dto.createdAt()).isNotNull();
            assertThat(dto.updatedAt()).isNotNull();
        }

        @Test
        void shouldPreserveIdAndTimestampsFromReconstructedVehicle() {
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 10, 10, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 15, 12, 0);
            Vehicle vehicle = Vehicle.reconstruct(VALID_ID, VALID_CUSTOMER_ID, new LicensePlate(VALID_OLD_PLATE),
                    VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, null, createdAt, updatedAt);

            VehicleResponseDTO dto = VehiclePresenter.present(vehicle);

            assertThat(dto.id()).isEqualTo(VALID_ID);
            assertThat(dto.createdAt()).isEqualTo(createdAt);
            assertThat(dto.updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        void shouldHaveNullIdForNewVehicle() {
            Vehicle vehicle = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(VALID_OLD_PLATE),
                    VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, null);

            VehicleResponseDTO dto = VehiclePresenter.present(vehicle);

            assertThat(dto.id()).isNull();
        }

        @Test
        void shouldAllowNullDescription() {
            Vehicle vehicle = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(VALID_OLD_PLATE),
                    VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, null);

            VehicleResponseDTO dto = VehiclePresenter.present(vehicle);

            assertThat(dto.description()).isNull();
        }
    }
}