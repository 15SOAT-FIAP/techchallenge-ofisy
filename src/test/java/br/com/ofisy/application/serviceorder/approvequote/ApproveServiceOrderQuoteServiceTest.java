package br.com.ofisy.application.serviceorder.approvequote;

import br.com.ofisy.application.quote.approve.ApproveQuoteUseCase;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ApproveServiceOrderQuoteServiceTest {

    public static final String PRICE_100 = "100.00";
    public static final String BARULHO = "Barulho";
    @Mock private ServiceOrderRepository serviceOrderRepository;
    @Mock private ApproveQuoteUseCase approveQuoteUseCase;

    @InjectMocks
    private ApproveServiceOrderQuoteService service;

    @Nested
    class Execute {

        @Test
        void shouldApproveQuoteAndUpdateServiceOrderSuccessfully() {
            UUID quoteId = UUID.randomUUID();
            UUID serviceOrderId = UUID.randomUUID();
            Quote quote = approvedQuote(quoteId, serviceOrderId);
            ServiceOrder serviceOrder = awaitingApprovalServiceOrder(serviceOrderId);

            when(approveQuoteUseCase.execute(quoteId)).thenReturn(quote);
            when(serviceOrderRepository.findById(serviceOrderId)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenReturn(serviceOrder);

            Quote result = service.execute(quoteId);

            assertThat(result).isEqualTo(quote);
            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_EXECUTION);
            verify(serviceOrderRepository).save(serviceOrder);
        }

        @Test
        void shouldThrowWhenServiceOrderNotFound() {
            UUID quoteId = UUID.randomUUID();
            UUID serviceOrderId = UUID.randomUUID();
            Quote quote = approvedQuote(quoteId, serviceOrderId);

            when(approveQuoteUseCase.execute(quoteId)).thenReturn(quote);
            when(serviceOrderRepository.findById(serviceOrderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(quoteId))
                    .isInstanceOf(ServiceOrderNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }
    }

    private Quote approvedQuote(UUID quoteId, UUID serviceOrderId) {
        Quote quote = Quote.reconstruct(quoteId, serviceOrderId, QuoteStatus.PENDING,
                new BigDecimal(PRICE_100), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
        quote.approve();
        return quote;
    }

    private ServiceOrder awaitingApprovalServiceOrder(UUID serviceOrderId) {
        ServiceOrder order = ServiceOrder.receive(UUID.randomUUID(), UUID.randomUUID(), BARULHO, UUID.randomUUID());
        order.startDiagnostic();
        order.sendToApproval();
        return order;
    }
}