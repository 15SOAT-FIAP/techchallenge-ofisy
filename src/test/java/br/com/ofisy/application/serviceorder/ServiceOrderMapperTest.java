package br.com.ofisy.application.serviceorder;

import br.com.ofisy.application.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderMapperTest {

    private static final UUID VEHICLE_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID CREATED_BY = UUID.randomUUID();
    private static final String REPORT = "Barulho na suspensão";

    @Nested
    class ToDomain {

        @Test
        void shouldMapRequestDTOToServiceOrder() {
            var request = new ServiceOrderRequestDTO(VEHICLE_ID, CUSTOMER_ID, REPORT);

            var result = ServiceOrderMapper.toDomain(request, CREATED_BY);

            assertThat(result.getVehicleId()).isEqualTo(VEHICLE_ID);
            assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(result.getReport()).isEqualTo(REPORT);
            assertThat(result.getCreatedBy()).isEqualTo(CREATED_BY);
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> ServiceOrderMapper.toDomain(null, CREATED_BY))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class ToResponseDTO {

        @Test
        void shouldMapServiceOrderToResponseDTO() {
            var order = ServiceOrder.receive(VEHICLE_ID, CUSTOMER_ID, REPORT, CREATED_BY);

            var result = ServiceOrderMapper.toResponseDTO(order);

            assertThat(result.vehicleId()).isEqualTo(VEHICLE_ID);
            assertThat(result.customerId()).isEqualTo(CUSTOMER_ID);
            assertThat(result.report()).isEqualTo(REPORT);
            assertThat(result.createdBy()).isEqualTo(CREATED_BY);
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.updatedAt()).isNotNull();
            assertThat(result.finishedAt()).isNull();
        }

        @Test
        void shouldMapStatusAsString() {
            var order = ServiceOrder.receive(VEHICLE_ID, CUSTOMER_ID, REPORT, CREATED_BY);

            var result = ServiceOrderMapper.toResponseDTO(order);

            assertThat(result.status()).isEqualTo(ServiceOrderStatus.RECEIVED.name());
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenServiceOrderIsNull() {
            assertThatThrownBy(() -> ServiceOrderMapper.toResponseDTO(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
