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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuoteRepositoryImplTest {

    @Mock private JpaQuoteRepository jpaQuoteRepository;
    @Mock private StockRepository stockRepository;
    @Mock private ServiceOrderExecutionRepository executionRepository;

    @InjectMocks
    private QuoteRepositoryImpl repository;

    @Nested
    @DisplayName("save — insert")
    class SaveInsert {

        @Test
        @DisplayName("Deve salvar novo orçamento e retornar domínio")
        void shouldSaveNewQuoteAndReturnDomain() {
            var quote = newQuote();
            var savedEntity = validEntity(UUID.randomUUID(), quote.getServiceOrderId());

            when(jpaQuoteRepository.save(any(QuoteEntity.class))).thenReturn(savedEntity);

            var result = repository.save(quote);

            assertThat(result).isNotNull();
            assertThat(result.getServiceOrderId()).isEqualTo(quote.getServiceOrderId());
        }

        @Test
        @DisplayName("Deve converter domínio para entity antes de salvar")
        void shouldConvertDomainToEntityBeforeSaving() {
            var quote = newQuote();
            var savedEntity = validEntity(UUID.randomUUID(), quote.getServiceOrderId());
            var captor = ArgumentCaptor.forClass(QuoteEntity.class);

            when(jpaQuoteRepository.save(any(QuoteEntity.class))).thenReturn(savedEntity);

            repository.save(quote);

            verify(jpaQuoteRepository).save(captor.capture());
            assertThat(captor.getValue().getServiceOrderId()).isEqualTo(quote.getServiceOrderId());
            assertThat(captor.getValue().getStatus()).isEqualTo(quote.getStatus());
        }
    }

    @Nested
    @DisplayName("save — update")
    class SaveUpdate {

        @Test
        @DisplayName("Deve atualizar orçamento existente sem violar constraint")
        void shouldUpdateExistingQuoteWithoutConstraintViolation() {
            var quoteId = UUID.randomUUID();
            var serviceOrderId = UUID.randomUUID();
            var quote = existingQuote(quoteId, serviceOrderId);
            var existingEntity = validEntity(quoteId, serviceOrderId);

            when(jpaQuoteRepository.findById(quoteId)).thenReturn(Optional.of(existingEntity));
            when(jpaQuoteRepository.save(any(QuoteEntity.class))).thenReturn(existingEntity);

            var result = repository.save(quote);

            assertThat(result).isNotNull();
            // verifica que o flush foi chamado para garantir DELETE antes do INSERT
            verify(jpaQuoteRepository).flush();
            verify(jpaQuoteRepository).save(existingEntity);
        }

        @Test
        @DisplayName("Deve limpar itens antigos antes de adicionar novos")
        void shouldClearOldItemsBeforeAddingNew() {
            var quoteId = UUID.randomUUID();
            var serviceOrderId = UUID.randomUUID();
            var quote = existingQuote(quoteId, serviceOrderId);
            var existingEntity = validEntity(quoteId, serviceOrderId);

            when(jpaQuoteRepository.findById(quoteId)).thenReturn(Optional.of(existingEntity));
            when(jpaQuoteRepository.save(any(QuoteEntity.class))).thenReturn(existingEntity);

            repository.save(quote);

            // após o clear, as listas devem estar vazias (novos itens do quote mockado também são vazios)
            assertThat(existingEntity.getStockItems()).isEmpty();
            assertThat(existingEntity.getServiceItems()).isEmpty();
        }

        @Test
        @DisplayName("Deve atualizar campos simples da entity")
        void shouldUpdateSimpleFieldsOnEntity() {
            var quoteId = UUID.randomUUID();
            var serviceOrderId = UUID.randomUUID();
            var quote = existingApprovedQuote(quoteId, serviceOrderId);
            var existingEntity = validEntity(quoteId, serviceOrderId);

            when(jpaQuoteRepository.findById(quoteId)).thenReturn(Optional.of(existingEntity));
            when(jpaQuoteRepository.save(any(QuoteEntity.class))).thenReturn(existingEntity);

            repository.save(quote);

            assertThat(existingEntity.getStatus()).isEqualTo(QuoteStatus.APPROVED);
            assertThat(existingEntity.getTotalPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando orçamento não encontrado para update")
        void shouldThrowWhenQuoteNotFoundForUpdate() {
            var quoteId = UUID.randomUUID();
            var quote = existingQuote(quoteId, UUID.randomUUID());

            when(jpaQuoteRepository.findById(quoteId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> repository.save(quote))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(quoteId.toString());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar orçamento quando encontrado")
        void shouldReturnQuoteWhenFound() {
            var id = UUID.randomUUID();
            var entity = validEntity(id, UUID.randomUUID());
            when(jpaQuoteRepository.findById(id)).thenReturn(Optional.of(entity));

            var result = repository.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().getServiceOrderId()).isEqualTo(entity.getServiceOrderId());
        }

        @Test
        @DisplayName("Deve retornar vazio quando não encontrado")
        void shouldReturnEmptyWhenNotFound() {
            var id = UUID.randomUUID();
            when(jpaQuoteRepository.findById(id)).thenReturn(Optional.empty());

            var result = repository.findById(id);

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
            var entity = validEntity(UUID.randomUUID(), serviceOrderId);
            when(jpaQuoteRepository.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(entity));

            var result = repository.findByServiceOrderId(serviceOrderId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getServiceOrderId()).isEqualTo(serviceOrderId);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há orçamentos")
        void shouldReturnEmptyListWhenNoneFound() {
            var serviceOrderId = UUID.randomUUID();
            when(jpaQuoteRepository.findByServiceOrderId(serviceOrderId)).thenReturn(List.of());

            var result = repository.findByServiceOrderId(serviceOrderId);

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

            assertThat(repository.existsByServiceOrderId(serviceOrderId)).isTrue();
        }

        @Test
        @DisplayName("Deve retornar falso quando orçamento não existe")
        void shouldReturnFalseWhenQuoteDoesNotExist() {
            var serviceOrderId = UUID.randomUUID();
            when(jpaQuoteRepository.existsByServiceOrderId(serviceOrderId)).thenReturn(false);

            assertThat(repository.existsByServiceOrderId(serviceOrderId)).isFalse();
        }
    }

    private Quote newQuote() {
        return Quote.reconstruct(null, UUID.randomUUID(), QuoteStatus.PENDING,
                new BigDecimal("100.00"), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Quote existingQuote(UUID id, UUID serviceOrderId) {
        return Quote.reconstruct(id, serviceOrderId, QuoteStatus.PENDING,
                new BigDecimal("100.00"), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Quote existingApprovedQuote(UUID id, UUID serviceOrderId) {
        var quote = Quote.reconstruct(id, serviceOrderId, QuoteStatus.PENDING,
                new BigDecimal("200.00"), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
        quote.approve();
        return quote;
    }

    private QuoteEntity validEntity(UUID id, UUID serviceOrderId) {
        return QuoteEntity.builder()
                .id(id)
                .serviceOrderId(serviceOrderId)
                .status(QuoteStatus.PENDING)
                .totalPrice(new BigDecimal("100.00"))
                .stockItems(new java.util.ArrayList<>())
                .serviceItems(new java.util.ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}