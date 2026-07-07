package br.com.ofisy.application.serviceorder.submitquoteforapproval;

import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
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
class SubmitQuoteForApprovalServiceTest {

    public static final String RELATORIO = "Relatório";
    public static final String PRICE_100 = "100.00";
    @Mock
    private ServiceOrderRepository serviceOrderRepository;
    @Mock
    private QuoteRepository quoteRepository;

    @InjectMocks
    private SubmitQuoteForApprovalService service;

    @Nested
    class Execute {

        @Test
        void shouldSubmitForApprovalSuccessfully() {
            var serviceOrderId = UUID.randomUUID();
            var serviceOrder = inDiagnosticServiceOrder(serviceOrderId);

            when(serviceOrderRepository.findById(serviceOrderId)).thenReturn(Optional.of(serviceOrder));
            when(quoteRepository.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(validQuote(serviceOrderId)));
            when(serviceOrderRepository.save(serviceOrder)).thenReturn(serviceOrder);

            var result = service.execute(serviceOrderId);

            assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
            verify(serviceOrderRepository).save(serviceOrder);
        }

        @Test
        void shouldThrowWhenServiceOrderNotFound() {
            var serviceOrderId = UUID.randomUUID();
            when(serviceOrderRepository.findById(serviceOrderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(serviceOrderId))
                    .isInstanceOf(ServiceOrderNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenNoQuoteExists() {
            var serviceOrderId = UUID.randomUUID();
            var serviceOrder = inDiagnosticServiceOrder(serviceOrderId);

            when(serviceOrderRepository.findById(serviceOrderId)).thenReturn(Optional.of(serviceOrder));
            when(quoteRepository.findByServiceOrderId(serviceOrderId)).thenReturn(List.of());

            assertThatThrownBy(() -> service.execute(serviceOrderId))
                    .isInstanceOf(QuoteNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenServiceOrderIsNotInDiagnostic() {
            var serviceOrderId = UUID.randomUUID();
            var serviceOrder = receivedServiceOrder(serviceOrderId);

            when(serviceOrderRepository.findById(serviceOrderId)).thenReturn(Optional.of(serviceOrder));
            when(quoteRepository.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(validQuote(serviceOrderId)));

            assertThatThrownBy(() -> service.execute(serviceOrderId))
                    .isInstanceOf(br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException.class);

            verify(serviceOrderRepository, never()).save(any());
        }
    }

    private ServiceOrder receivedServiceOrder(UUID id) {
        return ServiceOrder.reconstruct(id, UUID.randomUUID(), UUID.randomUUID(),
                RELATORIO, ServiceOrderStatus.RECEIVED, UUID.randomUUID(),
                LocalDateTime.now(), null, LocalDateTime.now());
    }

    private ServiceOrder inDiagnosticServiceOrder(UUID id) {
        var order = ServiceOrder.receive(UUID.randomUUID(), UUID.randomUUID(), RELATORIO, UUID.randomUUID());
        order.startDiagnostic();
        return order;
    }

    private Quote validQuote(UUID serviceOrderId) {
        return Quote.reconstruct(UUID.randomUUID(), serviceOrderId, QuoteStatus.PENDING,
                new BigDecimal(PRICE_100), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }
}