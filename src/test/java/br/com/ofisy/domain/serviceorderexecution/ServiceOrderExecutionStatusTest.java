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
    void shouldHaveAllExpectedStatuses() {
        var values = ServiceOrderExecutionStatus.values();
        assertThat(values).contains(
                ServiceOrderExecutionStatus.PENDING,
                ServiceOrderExecutionStatus.IN_PROGRESS,
                ServiceOrderExecutionStatus.COMPLETED,
                ServiceOrderExecutionStatus.CANCELLED
        );
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

    @Test
    void pendingShouldAllowInProgressAndCancelled() {
        assertThat(ServiceOrderExecutionStatus.PENDING.canTransitionTo(ServiceOrderExecutionStatus.IN_PROGRESS)).isTrue();
        assertThat(ServiceOrderExecutionStatus.PENDING.canTransitionTo(ServiceOrderExecutionStatus.CANCELLED)).isTrue();
        assertThat(ServiceOrderExecutionStatus.PENDING.canTransitionTo(ServiceOrderExecutionStatus.COMPLETED)).isFalse();
        assertThat(ServiceOrderExecutionStatus.PENDING.canTransitionTo(ServiceOrderExecutionStatus.PENDING)).isFalse();
    }

    @Test
    void inProgressShouldAllowCompletedAndCancelled() {
        assertThat(ServiceOrderExecutionStatus.IN_PROGRESS.canTransitionTo(ServiceOrderExecutionStatus.COMPLETED)).isTrue();
        assertThat(ServiceOrderExecutionStatus.IN_PROGRESS.canTransitionTo(ServiceOrderExecutionStatus.CANCELLED)).isTrue();
        assertThat(ServiceOrderExecutionStatus.IN_PROGRESS.canTransitionTo(ServiceOrderExecutionStatus.PENDING)).isFalse();
        assertThat(ServiceOrderExecutionStatus.IN_PROGRESS.canTransitionTo(ServiceOrderExecutionStatus.IN_PROGRESS)).isFalse();
    }

    @Test
    void completedShouldBeTerminal() {
        for (var next : ServiceOrderExecutionStatus.values()) {
            assertThat(ServiceOrderExecutionStatus.COMPLETED.canTransitionTo(next)).isFalse();
        }
    }

    @Test
    void cancelledShouldBeTerminal() {
        for (var next : ServiceOrderExecutionStatus.values()) {
            assertThat(ServiceOrderExecutionStatus.CANCELLED.canTransitionTo(next)).isFalse();
        }
    }
}