package br.com.ofisy.application.serviceorder.reprovequote;

import br.com.ofisy.application.quote.QuoteService;
import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.quote.dto.ReproveQuoteRequestDTO;
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
class ReproveServiceOrderQuoteServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final UUID VALID_SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderRepository serviceOrderRepository;
    @Mock
    private QuoteService quoteService;

    @InjectMocks
    private ReproveServiceOrderQuoteService reproveServiceOrderQuoteService;

    @Nested
    class Execute {

        @Test
        void shouldReproveQuoteSuccessfully() {
            UUID quoteId = UUID.randomUUID();
            ServiceOrder serviceOrder = serviceOrderAwaitingApproval();
            String reason = "Preço muito alto";
            ReproveQuoteRequestDTO request = new ReproveQuoteRequestDTO(reason);
            QuoteResponseDTO reprovedQuoteResponse = reprovedQuoteResponse(quoteId, reason);

            when(quoteService.reprove(quoteId, request)).thenReturn(reprovedQuoteResponse);
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenReturn(serviceOrder);

            QuoteResponseDTO result = reproveServiceOrderQuoteService.execute(
                    new ReproveServiceOrderQuoteUseCase.ReproveQuoteCommand(quoteId, request));

            assertThat(result).isEqualTo(reprovedQuoteResponse);
            assertThat(reprovedQuoteResponse.status()).isEqualTo(QuoteStatus.REPROVED);
            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
            verify(quoteService).reprove(quoteId, request);
            verify(serviceOrderRepository).save(serviceOrder);
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            UUID quoteId = UUID.randomUUID();
            ReproveQuoteRequestDTO request = new ReproveQuoteRequestDTO("Motivo");
            QuoteResponseDTO response = quoteResponse(quoteId);

            when(quoteService.reprove(quoteId, request)).thenReturn(response);
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reproveServiceOrderQuoteService.execute(
                    new ReproveServiceOrderQuoteUseCase.ReproveQuoteCommand(quoteId, request)))
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

    private QuoteResponseDTO reprovedQuoteResponse(UUID quoteId, String reason) {
        return new QuoteResponseDTO(quoteId, VALID_SERVICE_ORDER_ID, QuoteStatus.REPROVED,
                new BigDecimal("1500.00"), reason, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }
}