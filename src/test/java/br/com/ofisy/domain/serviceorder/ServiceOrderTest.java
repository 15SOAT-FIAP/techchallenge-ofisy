package br.com.ofisy.domain.serviceorder;

import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderTest {

    private static final UUID VEHICLE_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID CREATED_BY = UUID.randomUUID();
    private static final String REPORT = "Barulho na suspensão";

    @Nested
    class Receive {

        @Test
        void shouldCreateWithReceivedStatus() {
            var order = newOrder();

            assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        }

        @Test
        void shouldSetVehicleIdCustomerIdReportCreatedBy() {
            var order = newOrder();

            assertThat(order.getVehicleId()).isEqualTo(VEHICLE_ID);
            assertThat(order.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(order.getReport()).isEqualTo(REPORT);
            assertThat(order.getCreatedBy()).isEqualTo(CREATED_BY);
        }

        @Test
        void shouldSetCreatedAtAndUpdatedAtToNow() {
            var before = LocalDateTime.now();
            var order = newOrder();
            var after = LocalDateTime.now();

            assertThat(order.getCreatedAt()).isBetween(before, after);
            assertThat(order.getUpdatedAt()).isBetween(before, after);
        }

        @Test
        void shouldLeaveFinishedAtNull() {
            var order = newOrder();

            assertThat(order.getFinishedAt()).isNull();
        }

        @Test
        void shouldAllowNullReport() {
            assertThatNoException().isThrownBy(
                    () -> ServiceOrder.receive(VEHICLE_ID, CUSTOMER_ID, null, CREATED_BY));
        }
    }

    @Nested
    class LegalTransitions {

        @Test
        void receivedToInDiagnostic() {
            var order = orderIn(ServiceOrderStatus.RECEIVED);

            order.startDiagnostic();

            assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.IN_DIAGNOSTIC);
        }

        @Test
        void receivedToCancelled() {
            var order = orderIn(ServiceOrderStatus.RECEIVED);

            order.cancel();

            assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
        }

        @Test
        void inDiagnosticToAwaitingApproval() {
            var order = orderIn(ServiceOrderStatus.IN_DIAGNOSTIC);

            order.sendToApproval();

            assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
        }

        @Test
        void inDiagnosticToCancelled() {
            var order = orderIn(ServiceOrderStatus.IN_DIAGNOSTIC);

            order.cancel();

            assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
        }

        @Test
        void awaitingApprovalToAwaitingExecution() {
            var order = orderIn(ServiceOrderStatus.AWAITING_APPROVAL);

            order.approve();

            assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_EXECUTION);
        }

        @Test
        void awaitingApprovalToCancelled() {
            var order = orderIn(ServiceOrderStatus.AWAITING_APPROVAL);

            order.cancel();

            assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
        }

        @Test
        void awaitingExecutionToInProgress() {
            var order = orderIn(ServiceOrderStatus.AWAITING_EXECUTION);

            order.startExecution();

            assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.IN_PROGRESS);
        }

        @Test
        void awaitingExecutionToCancelled() {
            var order = orderIn(ServiceOrderStatus.AWAITING_EXECUTION);

            order.cancel();

            assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
        }

        @Test
        void inProgressToFinished() {
            var order = orderIn(ServiceOrderStatus.IN_PROGRESS);

            order.finish();

            assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.FINISHED);
        }

        @Test
        void inProgressToCancelled() {
            var order = orderIn(ServiceOrderStatus.IN_PROGRESS);

            order.cancel();

            assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
        }

        @Test
        void finishedToDelivered() {
            var order = orderIn(ServiceOrderStatus.FINISHED);

            order.deliver();

            assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.DELIVERED);
        }
    }

    @Nested
    class TransitionSideEffects {

        @Test
        void shouldUpdateUpdatedAtOnTransition() {
            var order = orderIn(ServiceOrderStatus.RECEIVED);
            var checkpoint = LocalDateTime.now();

            order.startDiagnostic();

            assertThat(order.getUpdatedAt()).isAfterOrEqualTo(checkpoint);
        }

        @Test
        void shouldSetFinishedAtWhenEnteringFinished() {
            var order = orderIn(ServiceOrderStatus.IN_PROGRESS);
            assertThat(order.getFinishedAt()).isNull();

            order.finish();

            assertThat(order.getFinishedAt()).isNotNull().isEqualTo(order.getUpdatedAt());
        }

        @Test
        void shouldNotSetFinishedAtOnOtherTransitions() {
            var order = orderIn(ServiceOrderStatus.AWAITING_EXECUTION);

            order.startExecution();

            assertThat(order.getFinishedAt()).isNull();
        }

        @Test
        void shouldNotSetFinishedAtWhenCancelled() {
            var order = orderIn(ServiceOrderStatus.IN_PROGRESS);

            order.cancel();

            assertThat(order.getFinishedAt()).isNull();
        }
    }

    @Nested
    class IllegalTransitions {

        @Test
        void cannotStartDiagnosticOutsideReceived() {
            assertRejected(ServiceOrderStatus.IN_DIAGNOSTIC, ServiceOrder::startDiagnostic);
            assertRejected(ServiceOrderStatus.AWAITING_APPROVAL, ServiceOrder::startDiagnostic);
            assertRejected(ServiceOrderStatus.AWAITING_EXECUTION, ServiceOrder::startDiagnostic);
            assertRejected(ServiceOrderStatus.IN_PROGRESS, ServiceOrder::startDiagnostic);
            assertRejected(ServiceOrderStatus.FINISHED, ServiceOrder::startDiagnostic);
            assertRejected(ServiceOrderStatus.DELIVERED, ServiceOrder::startDiagnostic);
            assertRejected(ServiceOrderStatus.CANCELLED, ServiceOrder::startDiagnostic);
        }

        @Test
        void cannotSendToApprovalOutsideInDiagnostic() {
            assertRejected(ServiceOrderStatus.RECEIVED, ServiceOrder::sendToApproval);
            assertRejected(ServiceOrderStatus.AWAITING_APPROVAL, ServiceOrder::sendToApproval);
            assertRejected(ServiceOrderStatus.AWAITING_EXECUTION, ServiceOrder::sendToApproval);
            assertRejected(ServiceOrderStatus.IN_PROGRESS, ServiceOrder::sendToApproval);
            assertRejected(ServiceOrderStatus.FINISHED, ServiceOrder::sendToApproval);
            assertRejected(ServiceOrderStatus.DELIVERED, ServiceOrder::sendToApproval);
            assertRejected(ServiceOrderStatus.CANCELLED, ServiceOrder::sendToApproval);
        }

        @Test
        void cannotApproveOutsideAwaitingApproval() {
            assertRejected(ServiceOrderStatus.RECEIVED, ServiceOrder::approve);
            assertRejected(ServiceOrderStatus.IN_DIAGNOSTIC, ServiceOrder::approve);
            assertRejected(ServiceOrderStatus.AWAITING_EXECUTION, ServiceOrder::approve);
            assertRejected(ServiceOrderStatus.IN_PROGRESS, ServiceOrder::approve);
            assertRejected(ServiceOrderStatus.FINISHED, ServiceOrder::approve);
            assertRejected(ServiceOrderStatus.DELIVERED, ServiceOrder::approve);
            assertRejected(ServiceOrderStatus.CANCELLED, ServiceOrder::approve);
        }

        @Test
        void cannotStartExecutionOutsideAwaitingExecution() {
            assertRejected(ServiceOrderStatus.RECEIVED, ServiceOrder::startExecution);
            assertRejected(ServiceOrderStatus.IN_DIAGNOSTIC, ServiceOrder::startExecution);
            assertRejected(ServiceOrderStatus.AWAITING_APPROVAL, ServiceOrder::startExecution);
            assertRejected(ServiceOrderStatus.IN_PROGRESS, ServiceOrder::startExecution);
            assertRejected(ServiceOrderStatus.FINISHED, ServiceOrder::startExecution);
            assertRejected(ServiceOrderStatus.DELIVERED, ServiceOrder::startExecution);
            assertRejected(ServiceOrderStatus.CANCELLED, ServiceOrder::startExecution);
        }

        @Test
        void cannotFinishOutsideInProgress() {
            assertRejected(ServiceOrderStatus.RECEIVED, ServiceOrder::finish);
            assertRejected(ServiceOrderStatus.IN_DIAGNOSTIC, ServiceOrder::finish);
            assertRejected(ServiceOrderStatus.AWAITING_APPROVAL, ServiceOrder::finish);
            assertRejected(ServiceOrderStatus.AWAITING_EXECUTION, ServiceOrder::finish);
            assertRejected(ServiceOrderStatus.FINISHED, ServiceOrder::finish);
            assertRejected(ServiceOrderStatus.DELIVERED, ServiceOrder::finish);
            assertRejected(ServiceOrderStatus.CANCELLED, ServiceOrder::finish);
        }

        @Test
        void cannotDeliverOutsideFinished() {
            assertRejected(ServiceOrderStatus.RECEIVED, ServiceOrder::deliver);
            assertRejected(ServiceOrderStatus.IN_DIAGNOSTIC, ServiceOrder::deliver);
            assertRejected(ServiceOrderStatus.AWAITING_APPROVAL, ServiceOrder::deliver);
            assertRejected(ServiceOrderStatus.AWAITING_EXECUTION, ServiceOrder::deliver);
            assertRejected(ServiceOrderStatus.IN_PROGRESS, ServiceOrder::deliver);
            assertRejected(ServiceOrderStatus.DELIVERED, ServiceOrder::deliver);
            assertRejected(ServiceOrderStatus.CANCELLED, ServiceOrder::deliver);
        }

        @Test
        void cannotCancelTerminalStates() {
            assertRejected(ServiceOrderStatus.FINISHED, ServiceOrder::cancel);
            assertRejected(ServiceOrderStatus.DELIVERED, ServiceOrder::cancel);
            assertRejected(ServiceOrderStatus.CANCELLED, ServiceOrder::cancel);
        }

        private void assertRejected(ServiceOrderStatus from, Consumer<ServiceOrder> action) {
            var order = orderIn(from);

            assertThatThrownBy(() -> action.accept(order))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);
            assertThat(order.getStatus()).isEqualTo(from);
        }
    }

    private static ServiceOrder newOrder() {
        return ServiceOrder.receive(VEHICLE_ID, CUSTOMER_ID, REPORT, CREATED_BY);
    }

    private static ServiceOrder orderIn(ServiceOrderStatus status) {
        var order = newOrder();
        switch (status) {
            case RECEIVED -> { /* já está */ }
            case IN_DIAGNOSTIC -> order.startDiagnostic();
            case AWAITING_APPROVAL -> {
                order.startDiagnostic();
                order.sendToApproval();
            }
            case AWAITING_EXECUTION -> {
                order.startDiagnostic();
                order.sendToApproval();
                order.approve();
            }
            case IN_PROGRESS -> {
                order.startDiagnostic();
                order.sendToApproval();
                order.approve();
                order.startExecution();
            }
            case FINISHED -> {
                order.startDiagnostic();
                order.sendToApproval();
                order.approve();
                order.startExecution();
                order.finish();
            }
            case DELIVERED -> {
                order.startDiagnostic();
                order.sendToApproval();
                order.approve();
                order.startExecution();
                order.finish();
                order.deliver();
            }
            case CANCELLED -> order.cancel();
        }
        return order;
    }
}