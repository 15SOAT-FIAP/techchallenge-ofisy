package br.com.ofisy.domain.serviceorderexecution;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderExecutionStatusTest {

    @Test
    void shouldHaveFourStatusValues() {
        var values = ServiceOrderExecutionStatus.values();
        assertThat(values).hasSize(4);
    }

    @Test
    void shouldHavePendingStatus() {
        var values = ServiceOrderExecutionStatus.values();
        assertThat(values).contains(ServiceOrderExecutionStatus.PENDING);
    }

    @Test
    void shouldHaveCompletedStatus() {
        var values = ServiceOrderExecutionStatus.values();
        assertThat(values).contains(ServiceOrderExecutionStatus.COMPLETED);
    }

    @Test
    void shouldHaveCancelledStatus() {
        var values = ServiceOrderExecutionStatus.values();
        assertThat(values).contains(ServiceOrderExecutionStatus.CANCELLED);
    }

    @Test
    void shouldHaveInProgressStatus() {
        var values = ServiceOrderExecutionStatus.values();
        assertThat(values).contains(ServiceOrderExecutionStatus.IN_PROGRESS);
    }

    @Test
    void shouldParseStatusFromString() {
        assertThat(ServiceOrderExecutionStatus.valueOf("PENDING"))
                .isEqualTo(ServiceOrderExecutionStatus.PENDING);
        assertThat(ServiceOrderExecutionStatus.valueOf("COMPLETED"))
                .isEqualTo(ServiceOrderExecutionStatus.COMPLETED);
        assertThat(ServiceOrderExecutionStatus.valueOf("CANCELLED"))
                .isEqualTo(ServiceOrderExecutionStatus.CANCELLED);
        assertThat(ServiceOrderExecutionStatus.valueOf("IN_PROGRESS"))
                .isEqualTo(ServiceOrderExecutionStatus.IN_PROGRESS);
    }

    @Test
    void shouldConvertToString() {
        assertThat(ServiceOrderExecutionStatus.PENDING.toString()).isEqualTo("PENDING");
        assertThat(ServiceOrderExecutionStatus.COMPLETED.toString()).isEqualTo("COMPLETED");
        assertThat(ServiceOrderExecutionStatus.CANCELLED.toString()).isEqualTo("CANCELLED");
        assertThat(ServiceOrderExecutionStatus.IN_PROGRESS.toString()).isEqualTo("IN_PROGRESS");
    }
}

