package br.com.ofisy.adapters.gateways.vehicle;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleRepositoryImplTest {

    private static final UUID VALID_ID = UUID.randomUUID();
    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final String VALID_PLATE = "ABC1234";

    @Mock
    private JpaVehicleRepository jpa;

    @InjectMocks
    private VehicleRepositoryImpl repository;

    @Nested
    class Save {

        @Test
        void shouldConvertToEntitySaveAndReturnDomain() {
            var vehicle = validVehicle();
            var entity = validEntity();
            when(jpa.save(org.mockito.ArgumentMatchers.any(VehicleEntity.class))).thenReturn(entity);

            var result = repository.save(vehicle);

            assertThat(result.getId()).isEqualTo(VALID_ID);
            assertThat(result.getLicensePlate().getValue()).isEqualTo(VALID_PLATE);
            verify(jpa).save(org.mockito.ArgumentMatchers.any(VehicleEntity.class));
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldDelegateToJpaAndMapToDomain() {
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(validEntity()), pageable, 1);
            when(jpa.findAll(pageable)).thenReturn(page);

            var result = repository.findAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getLicensePlate().getValue()).isEqualTo(VALID_PLATE);
            verify(jpa).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoVehicles() {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<VehicleEntity>(List.of(), pageable, 0);
            when(jpa.findAll(pageable)).thenReturn(emptyPage);

            var result = repository.findAll(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    class FindByCustomerId {

        @Test
        void shouldReturnMappedDomainList() {
            when(jpa.findByCustomerId(VALID_CUSTOMER_ID)).thenReturn(List.of(validEntity()));

            var result = repository.findByCustomerId(VALID_CUSTOMER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
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
        void shouldReturnMappedDomainWhenFound() {
            when(jpa.findById(VALID_ID)).thenReturn(Optional.of(validEntity()));

            var result = repository.findById(VALID_ID);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(VALID_ID);
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            when(jpa.findById(VALID_ID)).thenReturn(Optional.empty());

            var result = repository.findById(VALID_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByLicensePlate {

        @Test
        void shouldReturnMappedDomainWhenFound() {
            var licensePlate = new LicensePlate(VALID_PLATE);
            when(jpa.findByLicensePlate(VALID_PLATE)).thenReturn(Optional.of(validEntity()));

            var result = repository.findByLicensePlate(licensePlate);

            assertThat(result).isPresent();
            assertThat(result.get().getLicensePlate().getValue()).isEqualTo(VALID_PLATE);
            verify(jpa).findByLicensePlate(VALID_PLATE);
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            var licensePlate = new LicensePlate(VALID_PLATE);
            when(jpa.findByLicensePlate(VALID_PLATE)).thenReturn(Optional.empty());

            var result = repository.findByLicensePlate(licensePlate);

            assertThat(result).isEmpty();
        }
    }

    private Vehicle validVehicle() {
        return Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate(VALID_PLATE), "Civic", "Honda", "Preto", 2022, null);
    }

    private VehicleEntity validEntity() {
        return VehicleEntity.builder()
                .id(VALID_ID)
                .customerId(VALID_CUSTOMER_ID)
                .licensePlate(VALID_PLATE)
                .model("Civic")
                .brand("Honda")
                .color("Preto")
                .year(2022)
                .description(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}