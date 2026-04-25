package br.com.ofisy.domain.serviceOrderService;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderServiceStatusTest {

    @Test
    void shouldHaveFourStatusValues() {
        var values = ServiceOrderServiceStatus.values();
        assertThat(values).hasSize(4);
    }

    @Test
    void shouldHavePendingStatus() {
        var values = ServiceOrderServiceStatus.values();
        assertThat(values).contains(ServiceOrderServiceStatus.PENDING);
    }

    @Test
    void shouldHaveCompletedStatus() {
        var values = ServiceOrderServiceStatus.values();
        assertThat(values).contains(ServiceOrderServiceStatus.COMPLETED);
    }

    @Test
    void shouldHaveCancelledStatus() {
        var values = ServiceOrderServiceStatus.values();
        assertThat(values).contains(ServiceOrderServiceStatus.CANCELLED);
    }

    @Test
    void shouldHaveInProgressStatus() {
        var values = ServiceOrderServiceStatus.values();
        assertThat(values).contains(ServiceOrderServiceStatus.IN_PROGRESS);
    }

    @Test
    void shouldParseStatusFromString() {
        assertThat(ServiceOrderServiceStatus.valueOf("PENDING"))
                .isEqualTo(ServiceOrderServiceStatus.PENDING);
        assertThat(ServiceOrderServiceStatus.valueOf("COMPLETED"))
                .isEqualTo(ServiceOrderServiceStatus.COMPLETED);
        assertThat(ServiceOrderServiceStatus.valueOf("CANCELLED"))
                .isEqualTo(ServiceOrderServiceStatus.CANCELLED);
        assertThat(ServiceOrderServiceStatus.valueOf("IN_PROGRESS"))
                .isEqualTo(ServiceOrderServiceStatus.IN_PROGRESS);
    }

    @Test
    void shouldConvertToString() {
        assertThat(ServiceOrderServiceStatus.PENDING.toString()).isEqualTo("PENDING");
        assertThat(ServiceOrderServiceStatus.COMPLETED.toString()).isEqualTo("COMPLETED");
        assertThat(ServiceOrderServiceStatus.CANCELLED.toString()).isEqualTo("CANCELLED");
        assertThat(ServiceOrderServiceStatus.IN_PROGRESS.toString()).isEqualTo("IN_PROGRESS");
    }
}

