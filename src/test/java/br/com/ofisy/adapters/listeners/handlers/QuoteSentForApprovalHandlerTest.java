package br.com.ofisy.adapters.listeners.handlers;

import br.com.ofisy.adapters.listeners.dto.EventEnvelope;
import br.com.ofisy.application.serviceorder.awaitapproval.AwaitServiceOrderApprovalUseCase;
import br.com.ofisy.application.serviceorder.awaitapproval.AwaitServiceOrderApprovalUseCase.AwaitServiceOrderApprovalCommand;
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
class QuoteSentForApprovalHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private AwaitServiceOrderApprovalUseCase awaitServiceOrderApprovalUseCase;

    @Captor
    private ArgumentCaptor<AwaitServiceOrderApprovalCommand> commandCaptor;

    @InjectMocks
    private QuoteSentForApprovalHandler handler;

    @Test
    void shouldHandleQuoteSentForApprovalType() {
        assertThat(handler.type()).isEqualTo("QuoteSentForApproval");
    }

    @Nested
    class Handle {

        @Test
        void shouldMapEnvelopeToAwaitServiceOrderApprovalCommand() {
            EventEnvelope envelope = new EventEnvelope(EVENT_ID, QuoteSentForApprovalHandler.TYPE, SERVICE_ORDER_ID,
                    Instant.parse("2026-11-12T10:00:00Z"), JsonMapper.builder().build().readTree("{}"));

            handler.handle(envelope);

            verify(awaitServiceOrderApprovalUseCase).execute(commandCaptor.capture());
            AwaitServiceOrderApprovalCommand command = commandCaptor.getValue();
            assertThat(command.eventId()).isEqualTo(EVENT_ID);
            assertThat(command.eventType()).isEqualTo(QuoteSentForApprovalHandler.TYPE);
            assertThat(command.serviceOrderId()).isEqualTo(SERVICE_ORDER_ID);
        }
    }
}
