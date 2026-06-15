package br.com.ofisy.adapters.presenters.serviceorder;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderPresenterTest {

    private static final UUID VALID_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_CREATED_BY = UUID.randomUUID();
    private static final String VALID_REPORT = "Barulho na suspensão";
    private static final LocalDateTime NOW = LocalDateTime.of(2024, 1, 10, 10, 0);

    @Test
    void shouldMapAllFieldsFromDomainToResponseDTO() {
        var serviceOrder = ServiceOrder.reconstruct(VALID_ID, VALID_VEHICLE_ID, VALID_CUSTOMER_ID,
                VALID_REPORT, ServiceOrderStatus.RECEIVED, VALID_CREATED_BY, NOW, null, NOW);

        var result = ServiceOrderPresenter.present(serviceOrder);

        assertThat(result.id()).isEqualTo(VALID_ID);
        assertThat(result.vehicleId()).isEqualTo(VALID_VEHICLE_ID);
        assertThat(result.customerId()).isEqualTo(VALID_CUSTOMER_ID);
        assertThat(result.report()).isEqualTo(VALID_REPORT);
        assertThat(result.createdBy()).isEqualTo(VALID_CREATED_BY);
    }

    @Test
    void shouldMapStatusAsString() {
        var serviceOrder = ServiceOrder.reconstruct(VALID_ID, VALID_VEHICLE_ID, VALID_CUSTOMER_ID,
                VALID_REPORT, ServiceOrderStatus.IN_DIAGNOSTIC, VALID_CREATED_BY, NOW, null, NOW);

        var result = ServiceOrderPresenter.present(serviceOrder);

        assertThat(result.status()).isEqualTo("IN_DIAGNOSTIC");
    }

    @Test
    void shouldPreserveTimestamps() {
        var finishedAt = LocalDateTime.of(2024, 2, 1, 18, 0);
        var serviceOrder = ServiceOrder.reconstruct(VALID_ID, VALID_VEHICLE_ID, VALID_CUSTOMER_ID,
                VALID_REPORT, ServiceOrderStatus.FINISHED, VALID_CREATED_BY, NOW, finishedAt, NOW);

        var result = ServiceOrderPresenter.present(serviceOrder);

        assertThat(result.createdAt()).isEqualTo(NOW);
        assertThat(result.finishedAt()).isEqualTo(finishedAt);
        assertThat(result.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldPreserveNullFinishedAt() {
        var serviceOrder = ServiceOrder.reconstruct(VALID_ID, VALID_VEHICLE_ID, VALID_CUSTOMER_ID,
                VALID_REPORT, ServiceOrderStatus.RECEIVED, VALID_CREATED_BY, NOW, null, NOW);

        var result = ServiceOrderPresenter.present(serviceOrder);

        assertThat(result.finishedAt()).isNull();
    }
}