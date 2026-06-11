package br.com.ofisy.adapters.gateways.serviceorder;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderRepositoryImplTest {

    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_CREATED_BY = UUID.randomUUID();
    private static final String VALID_REPORT = "Barulho na suspensão";

    @Mock
    private JpaServiceOrderRepository jpa;

    @InjectMocks
    private ServiceOrderRepositoryImpl repository;

    @Nested
    class Save {

        @Test
        void shouldConvertToEntityBeforeSaving() {
            var serviceOrder = validServiceOrder();
            var savedEntity = validEntity();
            when(jpa.save(any(ServiceOrderEntity.class))).thenReturn(savedEntity);

            repository.save(serviceOrder);

            var captor = ArgumentCaptor.forClass(ServiceOrderEntity.class);
            verify(jpa).save(captor.capture());
            assertThat(captor.getValue().getVehicleId()).isEqualTo(VALID_VEHICLE_ID);
            assertThat(captor.getValue().getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(captor.getValue().getReport()).isEqualTo(VALID_REPORT);
            assertThat(captor.getValue().getCreatedBy()).isEqualTo(VALID_CREATED_BY);
            assertThat(captor.getValue().getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        }

        @Test
        void shouldReturnDomainServiceOrderWithIdAfterSave() {
            var serviceOrder = validServiceOrder();
            var savedEntity = validEntity();
            when(jpa.save(any(ServiceOrderEntity.class))).thenReturn(savedEntity);

            var result = repository.save(serviceOrder);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(savedEntity.getId());
            assertThat(result.getVehicleId()).isEqualTo(savedEntity.getVehicleId());
            assertThat(result.getCustomerId()).isEqualTo(savedEntity.getCustomerId());
            assertThat(result.getReport()).isEqualTo(savedEntity.getReport());
            assertThat(result.getStatus()).isEqualTo(savedEntity.getStatus());
        }

        @Test
        void shouldReturnServiceOrderWithTimestampsFromPersistedEntity() {
            var serviceOrder = validServiceOrder();
            var savedEntity = validEntity();
            when(jpa.save(any(ServiceOrderEntity.class))).thenReturn(savedEntity);

            var result = repository.save(serviceOrder);

            assertThat(result.getCreatedAt()).isEqualTo(savedEntity.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(savedEntity.getUpdatedAt());
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnDomainServiceOrderWhenEntityFound() {
            var id = UUID.randomUUID();
            var entity = validEntity();
            when(jpa.findById(id)).thenReturn(Optional.of(entity));

            var result = repository.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(entity.getId());
            assertThat(result.get().getVehicleId()).isEqualTo(entity.getVehicleId());
            assertThat(result.get().getStatus()).isEqualTo(entity.getStatus());
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
    class FindByStatus {

        @Test
        void shouldReturnPageOfDomainServiceOrdersMappedFromEntities() {
            var pageable = PageRequest.of(0, 10);
            var entity = validEntity();
            var page = new PageImpl<>(List.of(entity), pageable, 1);
            when(jpa.findByStatus(ServiceOrderStatus.RECEIVED, pageable)).thenReturn(page);

            var result = repository.findByStatus(ServiceOrderStatus.RECEIVED, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(entity.getId());
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(entity.getStatus());
            verify(jpa).findByStatus(ServiceOrderStatus.RECEIVED, pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoServiceOrdersMatchStatus() {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<ServiceOrderEntity>(List.of(), pageable, 0);
            when(jpa.findByStatus(ServiceOrderStatus.RECEIVED, pageable)).thenReturn(emptyPage);

            var result = repository.findByStatus(ServiceOrderStatus.RECEIVED, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    private ServiceOrder validServiceOrder() {
        return ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_CREATED_BY);
    }

    private ServiceOrderEntity validEntity() {
        return ServiceOrderEntity.builder()
                .id(UUID.randomUUID())
                .vehicleId(VALID_VEHICLE_ID)
                .customerId(VALID_CUSTOMER_ID)
                .report(VALID_REPORT)
                .status(ServiceOrderStatus.RECEIVED)
                .createdBy(VALID_CREATED_BY)
                .createdAt(LocalDateTime.now())
                .finishedAt(null)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}