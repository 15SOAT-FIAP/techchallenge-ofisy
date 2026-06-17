package br.com.ofisy.application.serviceorder.reprovequote;

import br.com.ofisy.application.quote.reprove.ReproveQuoteUseCase;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReproveServiceOrderQuoteServiceTest {

    @Mock private ServiceOrderRepository serviceOrderRepository;
    @Mock private ReproveQuoteUseCase reproveQuoteUseCase;

    @InjectMocks
    private ReproveServiceOrderQuoteService service;

    @Nested
    class Execute {

        @Test
        void shouldReproveQuoteAndCancelServiceOrderSuccessfully() {
            UUID quoteId = UUID.randomUUID();
            UUID serviceOrderId = UUID.randomUUID();
            String reason = "Preço muito alto";
            ReproveServiceOrderQuoteUseCase.ReproveServiceOrderQuoteCommand cmd = new ReproveServiceOrderQuoteUseCase.ReproveServiceOrderQuoteCommand(quoteId, reason);
            Quote quote = reprovedQuote(quoteId, serviceOrderId, reason);
            ServiceOrder serviceOrder = awaitingApprovalServiceOrder(serviceOrderId);

            when(reproveQuoteUseCase.execute(any(ReproveQuoteUseCase.ReproveQuoteCommand.class))).thenReturn(quote);
            when(serviceOrderRepository.findById(serviceOrderId)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenReturn(serviceOrder);

            var result = service.execute(cmd);

            assertThat(result).isEqualTo(quote);
            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
            verify(serviceOrderRepository).save(serviceOrder);
        }

        @Test
        void shouldPassQuoteIdAndReasonToInnerUseCase() {
            UUID quoteId = UUID.randomUUID();
            UUID serviceOrderId = UUID.randomUUID();
            String reason = "Cliente desistiu";
            ReproveServiceOrderQuoteUseCase.ReproveServiceOrderQuoteCommand cmd = new ReproveServiceOrderQuoteUseCase.ReproveServiceOrderQuoteCommand(quoteId, reason);
            Quote quote = reprovedQuote(quoteId, serviceOrderId, reason);
            ServiceOrder serviceOrder = awaitingApprovalServiceOrder(serviceOrderId);
            var captor = ArgumentCaptor.forClass(ReproveQuoteUseCase.ReproveQuoteCommand.class);

            when(reproveQuoteUseCase.execute(captor.capture())).thenReturn(quote);
            when(serviceOrderRepository.findById(serviceOrderId)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenReturn(serviceOrder);

            service.execute(cmd);

            assertThat(captor.getValue().id()).isEqualTo(quoteId);
            assertThat(captor.getValue().reason()).isEqualTo(reason);
        }

        @Test
        void shouldThrowWhenServiceOrderNotFound() {
            UUID quoteId = UUID.randomUUID();
            UUID serviceOrderId = UUID.randomUUID();
            ReproveServiceOrderQuoteUseCase.ReproveServiceOrderQuoteCommand cmd = new ReproveServiceOrderQuoteUseCase.ReproveServiceOrderQuoteCommand(quoteId, "Motivo");
            Quote quote = reprovedQuote(quoteId, serviceOrderId, "Motivo");

            when(reproveQuoteUseCase.execute(any())).thenReturn(quote);
            when(serviceOrderRepository.findById(serviceOrderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(ServiceOrderNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }
    }

    private Quote reprovedQuote(UUID quoteId, UUID serviceOrderId, String reason) {
        Quote quote = Quote.reconstruct(quoteId, serviceOrderId, QuoteStatus.PENDING,
                new BigDecimal("100.00"), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
        quote.reprove(reason);
        return quote;
    }

    private ServiceOrder awaitingApprovalServiceOrder(UUID serviceOrderId) {
        ServiceOrder order = ServiceOrder.receive(UUID.randomUUID(), UUID.randomUUID(), "Barulho", UUID.randomUUID());
        order.startDiagnostic();
        order.sendToApproval();
        return order;
    }
}