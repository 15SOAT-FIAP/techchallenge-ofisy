package br.com.ofisy.application.serviceorder.approvequote;

import br.com.ofisy.application.quote.QuoteService;
import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
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

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final UUID VALID_SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderRepository serviceOrderRepository;
    @Mock
    private QuoteService quoteService;

    @InjectMocks
    private ApproveServiceOrderQuoteService approveServiceOrderQuoteService;

    @Nested
    class Execute {

        @Test
        void shouldApproveQuoteSuccessfully() {
            UUID quoteId = UUID.randomUUID();
            ServiceOrder serviceOrder = serviceOrderAwaitingApproval();
            QuoteResponseDTO approvedQuoteResponse = approvedQuoteResponse(quoteId);

            when(quoteService.approve(quoteId)).thenReturn(approvedQuoteResponse);
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenReturn(serviceOrder);

            QuoteResponseDTO result = approveServiceOrderQuoteService.execute(quoteId);

            assertThat(result).isEqualTo(approvedQuoteResponse);
            assertThat(approvedQuoteResponse.status()).isEqualTo(QuoteStatus.APPROVED);
            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_EXECUTION);
            verify(quoteService).approve(quoteId);
            verify(serviceOrderRepository).save(serviceOrder);
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            UUID quoteId = UUID.randomUUID();
            QuoteResponseDTO response = quoteResponse(quoteId);

            when(quoteService.approve(quoteId)).thenReturn(response);
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> approveServiceOrderQuoteService.execute(quoteId))
                    .isInstanceOf(ServiceOrderNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }
    }

    private ServiceOrder serviceOrderAwaitingApproval() {
        ServiceOrder order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
        order.startDiagnostic();
        order.sendToApproval();
        return order;
    }

    private QuoteResponseDTO quoteResponse(UUID quoteId) {
        return new QuoteResponseDTO(quoteId, VALID_SERVICE_ORDER_ID, QuoteStatus.PENDING,
                new BigDecimal("1500.00"), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private QuoteResponseDTO approvedQuoteResponse(UUID quoteId) {
        return new QuoteResponseDTO(quoteId, VALID_SERVICE_ORDER_ID, QuoteStatus.APPROVED,
                new BigDecimal("1500.00"), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }
}