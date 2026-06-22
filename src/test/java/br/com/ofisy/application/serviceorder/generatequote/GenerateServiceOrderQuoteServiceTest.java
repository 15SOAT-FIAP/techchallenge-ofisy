package br.com.ofisy.application.serviceorder.generatequote;

import br.com.ofisy.application.notification.createquote.CreateQuoteNotificationUseCase;
import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.quote.Quote;
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
    public static final String PRICE_1500 = "1500.00";
    public static final String RELATORIO = "Relatório";

    @Mock
    private ServiceOrderRepository serviceOrderRepository;
    @Mock
    private CreateQuoteUseCase createQuoteUseCase;
    @Mock
    private CreateQuoteNotificationUseCase createQuoteNotificationUseCase;

    @InjectMocks
    private GenerateServiceOrderQuoteService generateServiceOrderQuoteService;

    @Nested
    class Execute {

        @Test
        void shouldGenerateQuoteSuccessfully() {
            ServiceOrder serviceOrder = inDiagnosticServiceOrder();
            GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand cmd = new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                    VALID_SERVICE_ORDER_ID, List.of(), List.of());
            UUID quoteId = UUID.randomUUID();
            Quote quote = validQuote(quoteId);

            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(createQuoteUseCase.execute(any())).thenReturn(quote);
            when(serviceOrderRepository.save(serviceOrder)).thenReturn(serviceOrder);

            Quote result = generateServiceOrderQuoteService.execute(cmd);

            assertThat(result).isEqualTo(quote);
            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
            verify(serviceOrderRepository).save(serviceOrder);
            verify(createQuoteNotificationUseCase).execute(
                    new CreateQuoteNotificationUseCase.CreateQuoteCommand(
                            quoteId, VALID_SERVICE_ORDER_ID, new BigDecimal(PRICE_1500)));
        }

        @Test
        void shouldPassCommandFieldsToCreateQuoteUseCase() {
            ServiceOrder serviceOrder = inDiagnosticServiceOrder();
            List<CreateQuoteUseCase.StockItemCommand> stockItems = List.of(new CreateQuoteUseCase.StockItemCommand(UUID.randomUUID(), 2));
            GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand cmd = new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                    VALID_SERVICE_ORDER_ID, stockItems, List.of());
            Quote quote = validQuote(UUID.randomUUID());

            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(createQuoteUseCase.execute(any())).thenReturn(quote);
            when(serviceOrderRepository.save(serviceOrder)).thenReturn(serviceOrder);

            generateServiceOrderQuoteService.execute(cmd);

            verify(createQuoteUseCase).execute(new CreateQuoteUseCase.CreateQuoteCommand(
                    VALID_SERVICE_ORDER_ID, stockItems, List.of()));
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand cmd = new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                    VALID_SERVICE_ORDER_ID, List.of(), List.of());

            assertThatThrownBy(() -> generateServiceOrderQuoteService.execute(cmd))
                    .isInstanceOf(ServiceOrderNotFoundException.class);

            verify(createQuoteUseCase, never()).execute(any());
            verify(createQuoteNotificationUseCase, never()).execute(any());
        }

        @Test
        void shouldThrowInvalidTransitionWhenOrderIsNotInDiagnostic() {
            ServiceOrder serviceOrder = receivedServiceOrder();
            GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand cmd = new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                    VALID_SERVICE_ORDER_ID, List.of(), List.of());
            Quote quote = validQuote(UUID.randomUUID());

            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(createQuoteUseCase.execute(any())).thenReturn(quote);

            assertThatThrownBy(() -> generateServiceOrderQuoteService.execute(cmd))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);

            verify(createQuoteNotificationUseCase, never()).execute(any());
        }
    }

    private ServiceOrder receivedServiceOrder() {
        return ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, RELATORIO, VALID_USER_ID);
    }

    private ServiceOrder inDiagnosticServiceOrder() {
        ServiceOrder order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, RELATORIO, VALID_USER_ID);
        order.startDiagnostic();
        return order;
    }

    private Quote validQuote(UUID quoteId) {
        return Quote.reconstruct(quoteId, VALID_SERVICE_ORDER_ID, QuoteStatus.PENDING,
                new BigDecimal(PRICE_1500), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }
}