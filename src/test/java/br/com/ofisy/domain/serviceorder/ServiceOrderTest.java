package br.com.ofisy.domain.serviceorder;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ServiceOrderTest {

    private static final UUID VEHICLE_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID CREATED_BY = UUID.randomUUID();
    private static final String REPORT = "Barulho na suspensão";

    @Nested
    class Receive {

        @Test
        void shouldCreateWithReceivedStatus() {
            var order = ServiceOrder.receive(VEHICLE_ID, CUSTOMER_ID, REPORT, CREATED_BY);

            assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        }

        @Test
        void shouldSetVehicleIdCustomerIdReportCreatedBy() {
            var order = ServiceOrder.receive(VEHICLE_ID, CUSTOMER_ID, REPORT, CREATED_BY);

            assertThat(order.getVehicleId()).isEqualTo(VEHICLE_ID);
            assertThat(order.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(order.getReport()).isEqualTo(REPORT);
            assertThat(order.getCreatedBy()).isEqualTo(CREATED_BY);
        }

        @Test
        void shouldSetCreatedAtAndUpdatedAtToNow() {
            var before = LocalDateTime.now();
            var order = ServiceOrder.receive(VEHICLE_ID, CUSTOMER_ID, REPORT, CREATED_BY);
            var after = LocalDateTime.now();

            assertThat(order.getCreatedAt()).isBetween(before, after);
            assertThat(order.getUpdatedAt()).isBetween(before, after);
        }

        @Test
        void shouldLeaveFinishedAtNull() {
            var order = ServiceOrder.receive(VEHICLE_ID, CUSTOMER_ID, REPORT, CREATED_BY);

            assertThat(order.getFinishedAt()).isNull();
        }

        @Test
        void shouldAllowNullReport() {
            assertThatNoException().isThrownBy(
                    () -> ServiceOrder.receive(VEHICLE_ID, CUSTOMER_ID, null, CREATED_BY));
        }
    }
}
