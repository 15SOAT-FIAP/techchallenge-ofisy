package br.com.ofisy.domain.vehicle;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final String VALID_OLD_PLATE = "ABC1234";
    private static final String VALID_MODEL = "Civic";
    private static final String VALID_BRAND = "Honda";
    private static final String VALID_COLOR = "Preto";
    private static final Integer VALID_YEAR = 2022;
    private static final String VALID_DESCRIPTION = "Barulho na suspensão";

    @Test
    void shouldCreateVehicleWithValidData() {
        var licensePlate = new LicensePlate(VALID_OLD_PLATE);

        var vehicle = Vehicle.create(VALID_CUSTOMER_ID, licensePlate, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

        assertThat(vehicle).isNotNull();
        assertThat(vehicle.getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
        assertThat(vehicle.getLicensePlate()).isEqualTo(licensePlate);
        assertThat(vehicle.getModel()).isEqualTo(VALID_MODEL);
        assertThat(vehicle.getBrand()).isEqualTo(VALID_BRAND);
        assertThat(vehicle.getColor()).isEqualTo(VALID_COLOR);
        assertThat(vehicle.getYear()).isEqualTo(VALID_YEAR);
        assertThat(vehicle.getDescription()).isEqualTo(VALID_DESCRIPTION);
    }

    @Test
    void shouldNotGenerateIdBeforePersistence() {
        var licensePlate = new LicensePlate(VALID_OLD_PLATE);

        var vehicle = Vehicle.create(VALID_CUSTOMER_ID, licensePlate, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

        assertThat(vehicle.getId()).isNull();
    }

    @Test
    void shouldSetCreatedAtOnCreation() {
        var licensePlate = new LicensePlate(VALID_OLD_PLATE);
        var before = LocalDateTime.now();

        var vehicle = Vehicle.create(VALID_CUSTOMER_ID, licensePlate, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

        var after = LocalDateTime.now();
        assertThat(vehicle.getCreatedAt()).isNotNull();
        assertThat(vehicle.getCreatedAt()).isBetween(before, after);
    }

    @Test
    void shouldSetUpdatedAtOnCreation() {
        var licensePlate = new LicensePlate(VALID_OLD_PLATE);
        var before = LocalDateTime.now();

        var vehicle = Vehicle.create(VALID_CUSTOMER_ID, licensePlate, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

        var after = LocalDateTime.now();
        assertThat(vehicle.getUpdatedAt()).isNotNull();
        assertThat(vehicle.getUpdatedAt()).isBetween(before, after);
    }

    @Test
    void shouldCreateVehicleWithNullCustomerId() {
        var licensePlate = new LicensePlate(VALID_OLD_PLATE);

        var vehicle = Vehicle.create(null, licensePlate, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

        assertThat(vehicle.getCustomerId()).isNull();
    }

    @Test
    void shouldCreateVehicleWithNullLicensePlate() {
        var vehicle = Vehicle.create(VALID_CUSTOMER_ID, null, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

        assertThat(vehicle.getLicensePlate()).isNull();
    }

    @Test
    void shouldCreateVehicleWithNullModel() {
        var licensePlate = new LicensePlate(VALID_OLD_PLATE);

        var vehicle = Vehicle.create(VALID_CUSTOMER_ID, licensePlate, null, VALID_BRAND, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

        assertThat(vehicle.getModel()).isNull();
    }

    @Test
    void shouldCreateVehicleWithNullBrand() {
        var licensePlate = new LicensePlate(VALID_OLD_PLATE);

        var vehicle = Vehicle.create(VALID_CUSTOMER_ID, licensePlate, VALID_MODEL, null, VALID_COLOR, VALID_YEAR, VALID_DESCRIPTION);

        assertThat(vehicle.getBrand()).isNull();
    }

    @Test
    void shouldCreateVehicleWithNullColor() {
        var licensePlate = new LicensePlate(VALID_OLD_PLATE);

        var vehicle = Vehicle.create(VALID_CUSTOMER_ID, licensePlate, VALID_MODEL, VALID_BRAND, null, VALID_YEAR, VALID_DESCRIPTION);

        assertThat(vehicle.getColor()).isNull();
    }

    @Test
    void shouldCreateVehicleWithNullYear() {
        var licensePlate = new LicensePlate(VALID_OLD_PLATE);

        var vehicle = Vehicle.create(VALID_CUSTOMER_ID, licensePlate, VALID_MODEL, VALID_BRAND, VALID_COLOR, null, VALID_DESCRIPTION);

        assertThat(vehicle.getYear()).isNull();
    }

    @Test
    void shouldCreateVehicleWithNullDescription() {
        var licensePlate = new LicensePlate(VALID_OLD_PLATE);

        var vehicle = Vehicle.create(VALID_CUSTOMER_ID, licensePlate, VALID_MODEL, VALID_BRAND, VALID_COLOR, VALID_YEAR, null);

        assertThat(vehicle.getDescription()).isNull();
    }
}
