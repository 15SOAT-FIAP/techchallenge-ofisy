package br.com.ofisy.application.quote;

import br.com.ofisy.application.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.quote.dto.ReproveQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.StockItemRequestDTO;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.application.stock.StockService;
import br.com.ofisy.application.stock.dto.StockResponseDTO;
import br.com.ofisy.domain.quote.*;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private StockService stockService;

    @Mock
    private StockRepository stockRepository;

    /* Comentando aqui até termos a parte de serviço
    @Mock
    private ServiceOrderExecutionRepository serviceOrderExecutionRepository;

    @Mock
    private ServiceRepository serviceRepository;
    */

    @Mock
    private QuoteMapper mapper;

    @InjectMocks
    private QuoteService quoteService;

    private QuoteResponseDTO mockResponse(Quote quote) {
        return new QuoteResponseDTO(
                quote.getId(),
                quote.getServiceOrderId(),
                quote.getStatus(),
                quote.getTotalPrice(),
                null,
                List.of(),
                List.of(),
                LocalDateTime.now(),
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar orçamento com itens de estoque com sucesso")
        void shouldCreateQuoteWithStockItemsSuccessfully() {
            Stock stock = mockStock();
            UUID stockId = stock.getId();

            Quote quote = mockQuote();
            QuoteResponseDTO response = mockResponse(quote);

            CreateQuoteRequestDTO request = new CreateQuoteRequestDTO(
                    UUID.randomUUID(),
                    List.of(new StockItemRequestDTO(stockId, 2)),
                    List.of()
            );

            when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
            when(quoteRepository.save(any())).thenReturn(quote);
            when(mapper.toResponse(quote)).thenReturn(response);

            var result = quoteService.create(request);

            assertThat(result).isNotNull();
            verify(stockService).consumeStock(stockId, 2);
            verify(quoteRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando item de estoque duplicado")
        void shouldThrowExceptionWhenDuplicateStockItem() {
            Stock stock = mockStock();
            UUID stockId = stock.getId();

            CreateQuoteRequestDTO request = new CreateQuoteRequestDTO(
                    UUID.randomUUID(),
                    List.of(
                            new StockItemRequestDTO(stockId, 2),
                            new StockItemRequestDTO(stockId, 1)
                    ),
                    List.of()
            );

            when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
            when(stockService.consumeStock(eq(stockId), anyInt()))
                    .thenReturn(mock(StockResponseDTO.class));

            assertThatThrownBy(() -> quoteService.create(request))
                    .isInstanceOf(QuoteItemAlreadyExistsException.class);

            verify(quoteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando estoque não encontrado")
        void shouldThrowExceptionWhenStockNotFound() {
            UUID stockId = UUID.randomUUID();

            CreateQuoteRequestDTO request = new CreateQuoteRequestDTO(
                    UUID.randomUUID(),
                    List.of(new StockItemRequestDTO(stockId, 2)),
                    List.of()
            );

            when(stockRepository.findById(stockId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> quoteService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(stockId.toString());

            verify(quoteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar orçamento quando encontrado")
        void shouldReturnQuoteWhenFound() {
            UUID id = UUID.randomUUID();
            Quote quote = mockQuote();
            QuoteResponseDTO response = mockResponse(quote);

            when(quoteRepository.findById(id)).thenReturn(Optional.of(quote));
            when(mapper.toResponse(quote)).thenReturn(response);

            var result = quoteService.findById(id);

            assertThat(result).isNotNull();
            verify(quoteRepository).findById(id);
        }

        @Test
        @DisplayName("Deve lançar exceção quando orçamento não encontrado")
        void shouldThrowExceptionWhenQuoteNotFound() {
            UUID id = UUID.randomUUID();
            when(quoteRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> quoteService.findById(id))
                    .isInstanceOf(QuoteNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findByServiceOrderId")
    class FindByServiceOrderId {

        @Test
        @DisplayName("Deve retornar lista de orçamentos da ordem de serviço")
        void shouldReturnQuotesByServiceOrderId() {
            UUID serviceOrderId = UUID.randomUUID();
            Quote quote = mockQuote();
            QuoteResponseDTO response = mockResponse(quote);

            when(quoteRepository.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(quote));
            when(mapper.toResponse(quote)).thenReturn(response);

            var result = quoteService.findByServiceOrderId(serviceOrderId);

            assertThat(result).hasSize(1);
            verify(quoteRepository).findByServiceOrderId(serviceOrderId);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há orçamentos")
        void shouldReturnEmptyListWhenNoQuotes() {
            UUID serviceOrderId = UUID.randomUUID();
            when(quoteRepository.findByServiceOrderId(serviceOrderId)).thenReturn(List.of());

            var result = quoteService.findByServiceOrderId(serviceOrderId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("approve")
    class Approve {

        @Test
        @DisplayName("Deve aprovar orçamento com sucesso")
        void shouldApproveQuoteSuccessfully() {
            UUID id = UUID.randomUUID();
            Quote quote = mockQuote();
            QuoteResponseDTO response = mockResponse(quote);

            when(quoteRepository.findById(id)).thenReturn(Optional.of(quote));
            when(quoteRepository.save(quote)).thenReturn(quote);
            when(mapper.toResponse(quote)).thenReturn(response);

            var result = quoteService.approve(id);

            assertThat(result).isNotNull();
            verify(quoteRepository).save(quote);
        }

        @Test
        @DisplayName("Deve lançar exceção ao aprovar orçamento não pendente")
        void shouldThrowExceptionWhenApprovingNonPendingQuote() {
            UUID id = UUID.randomUUID();
            Quote quote = mockQuote();
            quote.approve();

            when(quoteRepository.findById(id)).thenReturn(Optional.of(quote));

            assertThatThrownBy(() -> quoteService.approve(id))
                    .isInstanceOf(InvalidQuoteStatusException.class);

            verify(quoteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("reprove")
    class Reprove {

        @Test
        @DisplayName("Deve reprovar orçamento com sucesso")
        void shouldReproveQuoteSuccessfully() {
            UUID id = UUID.randomUUID();
            Quote quote = mockQuote();
            QuoteResponseDTO response = mockResponse(quote);
            ReproveQuoteRequestDTO request = new ReproveQuoteRequestDTO("Não gostei das avaliações da oficina no Google");

            when(quoteRepository.findById(id)).thenReturn(Optional.of(quote));
            when(quoteRepository.save(quote)).thenReturn(quote);
            when(mapper.toResponse(quote)).thenReturn(response);

            var result = quoteService.reprove(id, request);

            assertThat(result).isNotNull();
            verify(quoteRepository).save(quote);
        }

        @Test
        @DisplayName("Deve lançar exceção ao reprovar orçamento não pendente")
        void shouldThrowExceptionWhenReprovingNonPendingQuote() {
            UUID id = UUID.randomUUID();
            Quote quote = mockQuote();
            quote.approve();
            ReproveQuoteRequestDTO request = new ReproveQuoteRequestDTO("Motivo qualquer");

            when(quoteRepository.findById(id)).thenReturn(Optional.of(quote));

            assertThatThrownBy(() -> quoteService.reprove(id, request))
                    .isInstanceOf(InvalidQuoteStatusException.class);

            verify(quoteRepository, never()).save(any());
        }
    }

    private Stock mockStock() {
        Stock stock = Stock.create("Filtro de óleo", "Filtro", 10, new BigDecimal("100.00"), "Filtros", 2);
        ReflectionTestUtils.setField(stock, "id", UUID.randomUUID());
        return stock;
    }

    private Quote mockQuote() {
        Stock stock = mockStock();
        List<QuoteStockItem> stockItems = new ArrayList<>(List.of(QuoteStockItem.create(stock, 2)));
        return Quote.create(UUID.randomUUID(), stockItems, new ArrayList<>());
    }

    /* Comentando aqui até termos a parte de serviço
    private ServiceOrderExecution mockServiceOrderExecution(UUID serviceId) {
        return ServiceOrderExecution.create(serviceId, UUID.randomUUID());
    }

    private Service mockService(UUID id) {
        return Service.create("Troca de óleo", "Serviço de troca", new BigDecimal("150.00"));
    } */
}
