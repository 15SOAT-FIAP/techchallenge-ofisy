package br.com.ofisy.infrastructure.persistence.quote;

import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.stock.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteRepositoryImplTest {

    @Mock
    private JpaQuoteRepository jpaQuoteRepository;

    @InjectMocks
    private QuoteRepositoryImpl quoteRepository;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Deve salvar e retornar orçamento")
        void shouldSaveAndReturnQuote() {
            var quote = mockQuote();
            when(jpaQuoteRepository.save(quote)).thenReturn(quote);

            var result = quoteRepository.save(quote);

            assertThat(result).isEqualTo(quote);
            verify(jpaQuoteRepository).save(quote);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar orçamento quando encontrado")
        void shouldReturnQuoteWhenFound() {
            var id = UUID.randomUUID();
            var quote = mockQuote();
            when(jpaQuoteRepository.findById(id)).thenReturn(Optional.of(quote));

            var result = quoteRepository.findById(id);

            assertThat(result).isPresent();
            verify(jpaQuoteRepository).findById(id);
        }

        @Test
        @DisplayName("Deve retornar vazio quando não encontrado")
        void shouldReturnEmptyWhenNotFound() {
            var id = UUID.randomUUID();
            when(jpaQuoteRepository.findById(id)).thenReturn(Optional.empty());

            var result = quoteRepository.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByServiceOrderId")
    class FindByServiceOrderId {

        @Test
        @DisplayName("Deve retornar orçamentos da ordem de serviço")
        void shouldReturnQuotesByServiceOrderId() {
            var serviceOrderId = UUID.randomUUID();
            var quote = mockQuote();
            when(jpaQuoteRepository.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(quote));

            var result = quoteRepository.findByServiceOrderId(serviceOrderId);

            assertThat(result).hasSize(1);
            verify(jpaQuoteRepository).findByServiceOrderId(serviceOrderId);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há orçamentos")
        void shouldReturnEmptyListWhenNoQuotes() {
            var serviceOrderId = UUID.randomUUID();
            when(jpaQuoteRepository.findByServiceOrderId(serviceOrderId)).thenReturn(List.of());

            var result = quoteRepository.findByServiceOrderId(serviceOrderId);

            assertThat(result).isEmpty();
        }
    }

    private Quote mockQuote() {
        Stock stock = Stock.create("Filtro de óleo", "Filtro", 10, new BigDecimal("100.00"), "Filtros", 2);
        List<QuoteStockItem> items = List.of(QuoteStockItem.create(stock, 2));
        return Quote.create(UUID.randomUUID(), items);
    }
}
