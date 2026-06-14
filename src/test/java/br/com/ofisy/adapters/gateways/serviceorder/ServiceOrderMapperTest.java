package br.com.ofisy.adapters.gateways.serviceorder;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderMapperTest {

    private static final UUID VALID_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_CREATED_BY = UUID.randomUUID();
    private static final String VALID_REPORT = "Barulho na suspensão";

    @Nested
    class ToEntity {

        @Test
        void shouldMapAllFieldsFromDomainToEntity() {
            var serviceOrder = validReconstructed();

            var entity = ServiceOrderMapper.toEntity(serviceOrder);

            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(serviceOrder.getId());
            assertThat(entity.getVehicleId()).isEqualTo(serviceOrder.getVehicleId());
            assertThat(entity.getCustomerId()).isEqualTo(serviceOrder.getCustomerId());
            assertThat(entity.getReport()).isEqualTo(serviceOrder.getReport());
            assertThat(entity.getStatus()).isEqualTo(serviceOrder.getStatus());
            assertThat(entity.getCreatedBy()).isEqualTo(serviceOrder.getCreatedBy());
        }

        @Test
        void shouldPreserveNullIdForNewServiceOrder() {
            var serviceOrder = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_CREATED_BY);

            var entity = ServiceOrderMapper.toEntity(serviceOrder);

            assertThat(entity.getId()).isNull();
        }

        @Test
        void shouldPreserveIdForReconstructedServiceOrder() {
            var serviceOrder = validReconstructed();

            var entity = ServiceOrderMapper.toEntity(serviceOrder);

            assertThat(entity.getId()).isEqualTo(VALID_ID);
        }

        @Test
        void shouldPreserveNullFinishedAtWhenNotFinished() {
            var serviceOrder = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_CREATED_BY);

            var entity = ServiceOrderMapper.toEntity(serviceOrder);

            assertThat(entity.getFinishedAt()).isNull();
        }

        @Test
        void shouldPreserveTimestampsFromDomain() {
            var createdAt = LocalDateTime.of(2024, 1, 10, 10, 0);
            var finishedAt = LocalDateTime.of(2024, 1, 20, 18, 0);
            var updatedAt = LocalDateTime.of(2024, 1, 15, 12, 0);
            var serviceOrder = ServiceOrder.reconstruct(VALID_ID, VALID_VEHICLE_ID, VALID_CUSTOMER_ID,
                    VALID_REPORT, ServiceOrderStatus.FINISHED, VALID_CREATED_BY, createdAt, finishedAt, updatedAt);

            var entity = ServiceOrderMapper.toEntity(serviceOrder);

            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getFinishedAt()).isEqualTo(finishedAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldMapAllFieldsFromEntityToDomain() {
            var entity = validEntity();

            var serviceOrder = ServiceOrderMapper.toDomain(entity);

            assertThat(serviceOrder).isNotNull();
            assertThat(serviceOrder.getId()).isEqualTo(entity.getId());
            assertThat(serviceOrder.getVehicleId()).isEqualTo(entity.getVehicleId());
            assertThat(serviceOrder.getCustomerId()).isEqualTo(entity.getCustomerId());
            assertThat(serviceOrder.getReport()).isEqualTo(entity.getReport());
            assertThat(serviceOrder.getStatus()).isEqualTo(entity.getStatus());
            assertThat(serviceOrder.getCreatedBy()).isEqualTo(entity.getCreatedBy());
        }

        @Test
        void shouldPreserveTimestampsFromEntity() {
            var entity = validEntity();

            var serviceOrder = ServiceOrderMapper.toDomain(entity);

            assertThat(serviceOrder.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(serviceOrder.getFinishedAt()).isEqualTo(entity.getFinishedAt());
            assertThat(serviceOrder.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }

        @Test
        void shouldPreserveIdFromEntity() {
            var entity = validEntity();

            var serviceOrder = ServiceOrderMapper.toDomain(entity);

            assertThat(serviceOrder.getId()).isEqualTo(entity.getId());
        }

        @Test
        void shouldPreserveNullFinishedAtFromEntity() {
            var entity = ServiceOrderEntity.builder()
                    .id(VALID_ID)
                    .vehicleId(VALID_VEHICLE_ID)
                    .customerId(VALID_CUSTOMER_ID)
                    .report(VALID_REPORT)
                    .status(ServiceOrderStatus.RECEIVED)
                    .createdBy(VALID_CREATED_BY)
                    .createdAt(LocalDateTime.now())
                    .finishedAt(null)
                    .updatedAt(LocalDateTime.now())
                    .build();

            var serviceOrder = ServiceOrderMapper.toDomain(entity);

            assertThat(serviceOrder.getFinishedAt()).isNull();
        }
    }

    private ServiceOrderEntity validEntity() {
        return ServiceOrderEntity.builder()
                .id(VALID_ID)
                .vehicleId(VALID_VEHICLE_ID)
                .customerId(VALID_CUSTOMER_ID)
                .report(VALID_REPORT)
                .status(ServiceOrderStatus.RECEIVED)
                .createdBy(VALID_CREATED_BY)
                .createdAt(LocalDateTime.of(2024, 1, 10, 10, 0))
                .finishedAt(null)
                .updatedAt(LocalDateTime.of(2024, 1, 10, 10, 0))
                .build();
    }

    private ServiceOrder validReconstructed() {
        return ServiceOrder.reconstruct(
                VALID_ID, VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT,
                ServiceOrderStatus.RECEIVED, VALID_CREATED_BY,
                LocalDateTime.of(2024, 1, 10, 10, 0), null, LocalDateTime.of(2024, 1, 10, 10, 0));
    }
}