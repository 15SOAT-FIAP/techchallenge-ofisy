package br.com.ofisy.adapters.gateways.vehicle;

import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleGatewayMapperTest {

    private static final UUID VALID_ID = UUID.randomUUID();
    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final String VALID_OLD_PLATE = "ABC1234";
    private static final String VALID_MERCOSUL_PLATE = "ABC1D23";
    private static final String VALID_MODEL = "Civic";
    private static final String VALID_BRAND = "Honda";
    private static final String VALID_COLOR = "Preto";
    private static final Integer VALID_YEAR = 2022;
    private static final String VALID_DESCRIPTION = "Barulho na suspensão";

    @Nested
    class ToEntity {

        @ParameterizedTest
        @ValueSource(strings = {VALID_OLD_PLATE, VALID_MERCOSUL_PLATE})
        void shouldMapAllFieldsFromDomainToEntity(String plate) {
            var vehicle = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(plate),
                    VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

            var entity = VehicleMapper.toEntity(vehicle);

            assertThat(entity).isNotNull();
            assertThat(entity.getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(entity.getLicensePlate()).isEqualTo(plate);
            assertThat(entity.getModel()).isEqualTo(VALID_MODEL);
            assertThat(entity.getBrand()).isEqualTo(VALID_BRAND);
            assertThat(entity.getColor()).isEqualTo(VALID_COLOR);
            assertThat(entity.getYear()).isEqualTo(VALID_YEAR);
            assertThat(entity.getDescription()).isEqualTo(VALID_DESCRIPTION);
        }

        @Test
        void shouldPreserveNullIdForNewVehicle() {
            var vehicle = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(VALID_OLD_PLATE),
                    VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

            var entity = VehicleMapper.toEntity(vehicle);

            assertThat(entity.getId()).isNull();
        }

        @Test
        void shouldPreserveIdForReconstructedVehicle() {
            var vehicle = Vehicle.reconstruct(VALID_ID, VALID_CUSTOMER_ID, new LicensePlate(VALID_OLD_PLATE),
                    VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION,
                    LocalDateTime.now(), LocalDateTime.now());

            var entity = VehicleMapper.toEntity(vehicle);

            assertThat(entity.getId()).isEqualTo(VALID_ID);
        }

        @Test
        void shouldStoreLicensePlateAsRawStringValue() {
            var vehicle = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(VALID_OLD_PLATE),
                    VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

            var entity = VehicleMapper.toEntity(vehicle);

            assertThat(entity.getLicensePlate()).isEqualTo(VALID_OLD_PLATE);
            assertThat(entity.getLicensePlate()).matches("[A-Z]{3}\\d{4}");
        }

        @Test
        void shouldPreserveTimestampsFromDomain() {
            var createdAt = LocalDateTime.of(2024, 1, 10, 10, 0);
            var updatedAt = LocalDateTime.of(2024, 1, 15, 12, 0);
            var vehicle = Vehicle.reconstruct(VALID_ID, VALID_CUSTOMER_ID, new LicensePlate(VALID_OLD_PLATE),
                    VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION, createdAt, updatedAt);

            var entity = VehicleMapper.toEntity(vehicle);

            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        void shouldAllowNullDescription() {
            var vehicle = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(VALID_OLD_PLATE),
                    VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, null);

            var entity = VehicleMapper.toEntity(vehicle);

            assertThat(entity.getDescription()).isNull();
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldMapAllFieldsFromEntityToDomain() {
            var entity = validEntity();

            var vehicle = VehicleMapper.toDomain(entity);

            assertThat(vehicle).isNotNull();
            assertThat(vehicle.getId()).isEqualTo(entity.getId());
            assertThat(vehicle.getCustomerId()).isEqualTo(entity.getCustomerId());
            assertThat(vehicle.getModel()).isEqualTo(entity.getModel());
            assertThat(vehicle.getBrand()).isEqualTo(entity.getBrand());
            assertThat(vehicle.getColor()).isEqualTo(entity.getColor());
            assertThat(vehicle.getYear()).isEqualTo(entity.getYear());
            assertThat(vehicle.getDescription()).isEqualTo(entity.getDescription());
        }

        @ParameterizedTest
        @ValueSource(strings = {VALID_OLD_PLATE, VALID_MERCOSUL_PLATE})
        void shouldReconstructLicensePlateValueObjectFromString(String plate) {
            var entity = VehicleEntity.builder()
                    .id(VALID_ID)
                    .customerId(VALID_CUSTOMER_ID)
                    .licensePlate(plate)
                    .model(VALID_MODEL)
                    .brand(VALID_BRAND)
                    .color(VALID_COLOR)
                    .year(VALID_YEAR)
                    .description(VALID_DESCRIPTION)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            var vehicle = VehicleMapper.toDomain(entity);

            assertThat(vehicle.getLicensePlate()).isNotNull();
            assertThat(vehicle.getLicensePlate().getValue()).isEqualTo(plate);
        }

        @Test
        void shouldPreserveTimestampsFromEntity() {
            var entity = validEntity();

            var vehicle = VehicleMapper.toDomain(entity);

            assertThat(vehicle.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(vehicle.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }

        @Test
        void shouldPreserveIdFromEntity() {
            var entity = validEntity();

            var vehicle = VehicleMapper.toDomain(entity);

            assertThat(vehicle.getId()).isEqualTo(entity.getId());
        }

        @Test
        void shouldAllowNullDescription() {
            var entity = VehicleEntity.builder()
                    .id(VALID_ID)
                    .customerId(VALID_CUSTOMER_ID)
                    .licensePlate(VALID_OLD_PLATE)
                    .model(VALID_MODEL)
                    .brand(VALID_BRAND)
                    .color(VALID_COLOR)
                    .year(VALID_YEAR)
                    .description(null)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            var vehicle = VehicleMapper.toDomain(entity);

            assertThat(vehicle.getDescription()).isNull();
        }
    }

    private VehicleEntity validEntity() {
        return VehicleEntity.builder()
                .id(VALID_ID)
                .customerId(VALID_CUSTOMER_ID)
                .licensePlate(VALID_OLD_PLATE)
                .model(VALID_MODEL)
                .brand(VALID_BRAND)
                .color(VALID_COLOR)
                .year(VALID_YEAR)
                .description(VALID_DESCRIPTION)
                .createdAt(LocalDateTime.of(2024, 1, 10, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 12, 0))
                .build();
    }
}