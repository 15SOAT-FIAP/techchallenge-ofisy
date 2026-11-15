package br.com.ofisy.adapters.listeners.handlers;

import br.com.ofisy.adapters.listeners.dto.EventEnvelope;
import br.com.ofisy.application.serviceorder.confirmpayment.ConfirmServiceOrderPaymentUseCase;
import br.com.ofisy.application.serviceorder.confirmpayment.ConfirmServiceOrderPaymentUseCase.ConfirmServiceOrderPaymentCommand;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentConfirmedHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private ConfirmServiceOrderPaymentUseCase confirmServiceOrderPaymentUseCase;

    @Captor
    private ArgumentCaptor<ConfirmServiceOrderPaymentCommand> commandCaptor;

    @InjectMocks
    private PaymentConfirmedHandler handler;

    @Test
    void shouldHandlePaymentConfirmedType() {
        assertThat(handler.type()).isEqualTo("PaymentConfirmed");
    }

    @Nested
    class Handle {

        @Test
        void shouldMapEnvelopeToConfirmServiceOrderPaymentCommand() {
            EventEnvelope envelope = new EventEnvelope(EVENT_ID, PaymentConfirmedHandler.TYPE, SERVICE_ORDER_ID,
                    Instant.parse("2026-11-02T15:10:00.123456Z"),
                    JsonMapper.builder().build().readTree(
                            "{\"paymentId\":\"%s\",\"amount\":250.00}".formatted(UUID.randomUUID())));

            handler.handle(envelope);

            verify(confirmServiceOrderPaymentUseCase).execute(commandCaptor.capture());
            ConfirmServiceOrderPaymentCommand command = commandCaptor.getValue();
            assertThat(command.eventId()).isEqualTo(EVENT_ID);
            assertThat(command.eventType()).isEqualTo(PaymentConfirmedHandler.TYPE);
            assertThat(command.serviceOrderId()).isEqualTo(SERVICE_ORDER_ID);
        }
    }
}
