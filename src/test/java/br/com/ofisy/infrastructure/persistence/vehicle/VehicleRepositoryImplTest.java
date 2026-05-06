package br.com.ofisy.infrastructure.persistence.vehicle;

import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleRepositoryImplTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final String VALID_PLATE = "ABC1234";

    @Mock
    private JpaVehicleRepository jpa;

    @InjectMocks
    private VehicleRepositoryImpl repository;

    @Nested
    class Save {

        @Test
        void shouldDelegateToJpaAndReturnSavedVehicle() {
            var vehicle = validVehicle();
            when(jpa.save(vehicle)).thenReturn(vehicle);

            var result = repository.save(vehicle);

            assertThat(result).isSameAs(vehicle);
            verify(jpa).save(vehicle);
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldDelegateToJpaWithPageable() {
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(validVehicle()), pageable, 1);
            when(jpa.findAll(pageable)).thenReturn(page);

            var result = repository.findAll(pageable);

            assertThat(result).isEqualTo(page);
            verify(jpa).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoVehicles() {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<Vehicle>(List.of(), pageable, 0);
            when(jpa.findAll(pageable)).thenReturn(emptyPage);

            var result = repository.findAll(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    class FindByCustomerId {

        @Test
        void shouldReturnListOfVehicles() {
            var vehicle = validVehicle();
            when(jpa.findByCustomerId(VALID_CUSTOMER_ID)).thenReturn(List.of(vehicle));

            var result = repository.findByCustomerId(VALID_CUSTOMER_ID);

            assertThat(result).hasSize(1).contains(vehicle);
            verify(jpa).findByCustomerId(VALID_CUSTOMER_ID);
        }

        @Test
        void shouldReturnEmptyListWhenNoneFound() {
            when(jpa.findByCustomerId(VALID_CUSTOMER_ID)).thenReturn(List.of());

            var result = repository.findByCustomerId(VALID_CUSTOMER_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnVehicleWhenFound() {
            var id = UUID.randomUUID();
            var vehicle = validVehicle();
            when(jpa.findById(id)).thenReturn(Optional.of(vehicle));

            var result = repository.findById(id);

            assertThat(result).isPresent().contains(vehicle);
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            var id = UUID.randomUUID();
            when(jpa.findById(id)).thenReturn(Optional.empty());

            var result = repository.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByLicensePlate {

        @Test
        void shouldReturnVehicleWhenFound() {
            var licensePlate = new LicensePlate(VALID_PLATE);
            var vehicle = validVehicle();
            when(jpa.findByLicensePlateValue(VALID_PLATE)).thenReturn(Optional.of(vehicle));

            var result = repository.findByLicensePlate(licensePlate);

            assertThat(result).isPresent().contains(vehicle);
            verify(jpa).findByLicensePlateValue(VALID_PLATE);
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            var licensePlate = new LicensePlate(VALID_PLATE);
            when(jpa.findByLicensePlateValue(VALID_PLATE)).thenReturn(Optional.empty());

            var result = repository.findByLicensePlate(licensePlate);

            assertThat(result).isEmpty();
        }
    }

    private Vehicle validVehicle() {
        return Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(VALID_PLATE), "Civic", "Honda", "Preto", 2022, null);
    }
}
