package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.stock.StockRepository;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuoteRepositoryImplTest {

    public static final String PRICE_100 = "100.00";
    @Mock
    private JpaQuoteRepository jpaQuoteRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private ServiceOrderExecutionRepository serviceOrderExecutionRepository;

    @InjectMocks
    private QuoteRepositoryImpl quoteRepository;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Deve salvar e retornar orçamento")
        void shouldSaveAndReturnQuote() {
            var quote = mockQuote();
            var savedEntity = mockEntity(quote.getServiceOrderId());
            when(jpaQuoteRepository.save(any(QuoteEntity.class))).thenReturn(savedEntity);

            var result = quoteRepository.save(quote);

            assertThat(result).isNotNull();
            assertThat(result.getServiceOrderId()).isEqualTo(quote.getServiceOrderId());
        }

        @Test
        @DisplayName("Deve converter orçamento de domínio para entidade antes de salvar")
        void shouldConvertDomainToEntityBeforeSaving() {
            var quote = mockQuote();
            var savedEntity = mockEntity(quote.getServiceOrderId());
            var captor = ArgumentCaptor.forClass(QuoteEntity.class);
            when(jpaQuoteRepository.save(any(QuoteEntity.class))).thenReturn(savedEntity);

            quoteRepository.save(quote);

            verify(jpaQuoteRepository).save(captor.capture());
            assertThat(captor.getValue().getServiceOrderId()).isEqualTo(quote.getServiceOrderId());
            assertThat(captor.getValue().getStatus()).isEqualTo(quote.getStatus());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar orçamento quando encontrado")
        void shouldReturnQuoteWhenFound() {
            var id = UUID.randomUUID();
            var entity = mockEntity(UUID.randomUUID());
            when(jpaQuoteRepository.findById(id)).thenReturn(Optional.of(entity));

            var result = quoteRepository.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().getServiceOrderId()).isEqualTo(entity.getServiceOrderId());
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
            var entity = mockEntity(serviceOrderId);
            when(jpaQuoteRepository.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(entity));

            var result = quoteRepository.findByServiceOrderId(serviceOrderId);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getServiceOrderId()).isEqualTo(serviceOrderId);
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

    @Nested
    @DisplayName("existsByServiceOrderId")
    class ExistsByServiceOrderId {

        @Test
        @DisplayName("Deve retornar verdadeiro quando orçamento existe")
        void shouldReturnTrueWhenQuoteExists() {
            var serviceOrderId = UUID.randomUUID();
            when(jpaQuoteRepository.existsByServiceOrderId(serviceOrderId)).thenReturn(true);

            var result = quoteRepository.existsByServiceOrderId(serviceOrderId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Deve retornar falso quando orçamento não existe")
        void shouldReturnFalseWhenQuoteDoesNotExist() {
            var serviceOrderId = UUID.randomUUID();
            when(jpaQuoteRepository.existsByServiceOrderId(serviceOrderId)).thenReturn(false);

            var result = quoteRepository.existsByServiceOrderId(serviceOrderId);

            assertThat(result).isFalse();
        }
    }

    private Quote mockQuote() {
        return Quote.reconstruct(null, UUID.randomUUID(), QuoteStatus.PENDING,
                new BigDecimal(PRICE_100), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private QuoteEntity mockEntity(UUID serviceOrderId) {
        return QuoteEntity.builder()
                .id(UUID.randomUUID())
                .serviceOrderId(serviceOrderId)
                .status(QuoteStatus.PENDING)
                .totalPrice(new BigDecimal(PRICE_100))
                .stockItems(List.of())
                .serviceItems(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
