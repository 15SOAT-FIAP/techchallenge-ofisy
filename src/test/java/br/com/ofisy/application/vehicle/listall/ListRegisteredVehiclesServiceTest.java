package br.com.ofisy.application.vehicle.listall;

import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListRegisteredVehiclesServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private ListRegisteredVehiclesService listRegisteredVehiclesService;

    @Nested
    class ListRegisteredVehicles {

        @Test
        void shouldReturnPageOfVehicles() {
            Pageable pageable = PageRequest.of(0, 10);
            Vehicle vehicle = Vehicle.create(VALID_CUSTOMER_ID, new LicensePlate("ABC1234"),
                    "Civic", "Honda", "Preto", 2022, null);
            Page<Vehicle> page = new PageImpl<>(List.of(vehicle), pageable, 1);
            when(vehicleRepository.findAll(pageable)).thenReturn(page);

            Page<Vehicle> result = listRegisteredVehiclesService.execute(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(vehicleRepository).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoVehicles() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Vehicle> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(vehicleRepository.findAll(pageable)).thenReturn(emptyPage);

            Page<Vehicle> result = listRegisteredVehiclesService.execute(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        void shouldDelegateToRepository() {
            Pageable pageable = PageRequest.of(0, 10);
            when(vehicleRepository.findAll(pageable)).thenReturn(Page.empty());

            listRegisteredVehiclesService.execute(pageable);

            verify(vehicleRepository).findAll(pageable);
        }
    }
}