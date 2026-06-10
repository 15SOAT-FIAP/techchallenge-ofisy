package br.com.ofisy.adapters.presenters.serviceorder;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderStatusPresenterTest {

    private static final UUID VALID_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_CREATED_BY = UUID.randomUUID();
    private static final LocalDateTime NOW = LocalDateTime.of(2024, 1, 10, 10, 0);

    @Test
    void shouldMapAllFieldsFromDomainToStatusResponseDTO() {
        var serviceOrder = ServiceOrder.reconstruct(VALID_ID, VALID_VEHICLE_ID, VALID_CUSTOMER_ID,
                "report", ServiceOrderStatus.RECEIVED, VALID_CREATED_BY, NOW, null, NOW);

        var result = ServiceOrderStatusPresenter.present(serviceOrder);

        assertThat(result.id()).isEqualTo(VALID_ID);
        assertThat(result.vehicleId()).isEqualTo(VALID_VEHICLE_ID);
        assertThat(result.customerId()).isEqualTo(VALID_CUSTOMER_ID);
        assertThat(result.status()).isEqualTo(ServiceOrderStatus.RECEIVED);
        assertThat(result.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldPreserveStatusEnum() {
        var serviceOrder = ServiceOrder.reconstruct(VALID_ID, VALID_VEHICLE_ID, VALID_CUSTOMER_ID,
                "report", ServiceOrderStatus.CANCELLED, VALID_CREATED_BY, NOW, null, NOW);

        var result = ServiceOrderStatusPresenter.present(serviceOrder);

        assertThat(result.status()).isEqualTo(ServiceOrderStatus.CANCELLED);
    }
}