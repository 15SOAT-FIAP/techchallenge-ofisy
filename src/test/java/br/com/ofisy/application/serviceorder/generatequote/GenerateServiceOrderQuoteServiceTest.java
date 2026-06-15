package br.com.ofisy.application.serviceorder.generatequote;

import br.com.ofisy.application.notification.createquote.CreateQuoteNotificationUseCase;
import br.com.ofisy.application.quote.QuoteService;
import br.com.ofisy.application.quote.dto.*;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
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
class GenerateServiceOrderQuoteServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final UUID VALID_SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderRepository serviceOrderRepository;
    @Mock
    private QuoteService quoteService;
    @Mock
    private CreateQuoteNotificationUseCase createQuoteNotificationUseCase;

    @InjectMocks
    private GenerateServiceOrderQuoteService generateServiceOrderQuoteService;

    @Nested
    class Execute {

        @Test
        void shouldGenerateQuoteSuccessfully() {
            ServiceOrder serviceOrder = inDiagnosticServiceOrder();
            CreateQuoteRequestDTO request = quoteRequest();
            UUID quoteId = UUID.randomUUID();
            QuoteResponseDTO quote = quoteResponse(quoteId);

            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(quoteService.create(VALID_SERVICE_ORDER_ID, request)).thenReturn(quote);
            when(serviceOrderRepository.save(serviceOrder)).thenReturn(serviceOrder);

            QuoteResponseDTO result = generateServiceOrderQuoteService.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(VALID_SERVICE_ORDER_ID, request));

            assertThat(result).isEqualTo(quote);
            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
            verify(serviceOrderRepository).save(serviceOrder);
            verify(createQuoteNotificationUseCase).execute(
                    new CreateQuoteNotificationUseCase.CreateQuoteCommand(quoteId, VALID_SERVICE_ORDER_ID, new BigDecimal("1500.00")));
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            CreateQuoteRequestDTO request = quoteRequest();
            assertThatThrownBy(() -> generateServiceOrderQuoteService.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(VALID_SERVICE_ORDER_ID, request)))
                    .isInstanceOf(ServiceOrderNotFoundException.class);

            verify(quoteService, never()).create(any(), any());
            verify(createQuoteNotificationUseCase, never()).execute(any());
        }

        @Test
        void shouldThrowInvalidTransitionWhenOrderIsNotInDiagnostic() {
            ServiceOrder serviceOrder = receivedServiceOrder();
            CreateQuoteRequestDTO request = quoteRequest();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(quoteService.create(VALID_SERVICE_ORDER_ID, request)).thenReturn(quoteResponse(UUID.randomUUID()));

            assertThatThrownBy(() -> generateServiceOrderQuoteService.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(VALID_SERVICE_ORDER_ID, request)))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);

            verify(createQuoteNotificationUseCase, never()).execute(any());
        }
    }

    private ServiceOrder receivedServiceOrder() {
        return ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
    }

    private ServiceOrder inDiagnosticServiceOrder() {
        ServiceOrder order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
        order.startDiagnostic();
        return order;
    }

    private CreateQuoteRequestDTO quoteRequest() {
        return new CreateQuoteRequestDTO(List.of(), List.of());
    }

    private QuoteResponseDTO quoteResponse(UUID quoteId) {
        return new QuoteResponseDTO(quoteId, VALID_SERVICE_ORDER_ID, QuoteStatus.PENDING,
                new BigDecimal("1500.00"), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }
}