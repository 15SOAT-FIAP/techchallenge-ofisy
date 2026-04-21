package br.com.ofisy.application.vehicle;

import br.com.ofisy.application.vehicle.dto.VehicleRequestDTO;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleMapperTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final String VALID_MODEL = "Civic";
    private static final String VALID_BRAND = "Honda";
    private static final String VALID_COLOR = "Preto";
    private static final Integer VALID_YEAR = 2022;
    private static final String VALID_DESCRIPTION = "Barulho na suspensão";

    @Nested
    class ToDomain {

        @ParameterizedTest
        @ValueSource(strings = {"ABC1234", "ABC1D23"})
        void shouldMapRequestToDomainWithValidPlate(String plate) {
            var request = new VehicleRequestDTO(VALID_CUSTOMER_ID, plate, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

            var vehicle = VehicleMapper.toDomain(request);

            assertThat(vehicle).isNotNull();
            assertThat(vehicle.getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(vehicle.getLicensePlate().getValue()).isEqualTo(plate);
            assertThat(vehicle.getModel()).isEqualTo(VALID_MODEL);
            assertThat(vehicle.getBrand()).isEqualTo(VALID_BRAND);
            assertThat(vehicle.getColor()).isEqualTo(VALID_COLOR);
            assertThat(vehicle.getYear()).isEqualTo(VALID_YEAR);
            assertThat(vehicle.getDescription()).isEqualTo(VALID_DESCRIPTION);
        }

        @Test
        void shouldMapRequestWithNullDescriptionToDomain() {
            var request = new VehicleRequestDTO(VALID_CUSTOMER_ID, "ABC1234", VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, null);

            var vehicle = VehicleMapper.toDomain(request);

            assertThat(vehicle.getDescription()).isNull();
        }

        @Test
        void shouldThrowWhenLicensePlateIsNull() {
            var request = new VehicleRequestDTO(VALID_CUSTOMER_ID, null, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

            assertThatThrownBy(() -> VehicleMapper.toDomain(request))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class ToDTO {

        @ParameterizedTest
        @ValueSource(strings = {"ABC1234", "ABC1D23"})
        void shouldMapDomainToDTOWithValidPlate(String plate) {
            var vehicle = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(plate), VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

            var dto = VehicleMapper.toDTO(vehicle);

            assertThat(dto).isNotNull();
            assertThat(dto.id()).isNull();
            assertThat(dto.customerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(dto.licensePlate()).isEqualTo(plate);
            assertThat(dto.model()).isEqualTo(VALID_MODEL);
            assertThat(dto.brand()).isEqualTo(VALID_BRAND);
            assertThat(dto.color()).isEqualTo(VALID_COLOR);
            assertThat(dto.year()).isEqualTo(VALID_YEAR);
            assertThat(dto.description()).isEqualTo(VALID_DESCRIPTION);
            assertThat(dto.createdAt()).isNotNull();
            assertThat(dto.updatedAt()).isNotNull();
        }

        @Test
        void shouldMapDomainWithNullDescriptionToDTO() {
            var vehicle = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate("ABC1234"), VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, null);

            var dto = VehicleMapper.toDTO(vehicle);

            assertThat(dto.description()).isNull();
        }
    }
}
