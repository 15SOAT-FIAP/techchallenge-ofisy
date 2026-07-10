package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import br.com.ofisy.domain.stock.Stock;
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
import java.util.ArrayList;
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

    private static final UUID STOCK_ID = UUID.randomUUID();
    private static final UUID STOCK_ID_2 = UUID.randomUUID();
    private static final String PRICE_100 = "100.00";
    private static final String PRICE_150 = "150.00";

    @Mock private JpaQuoteRepository jpaQuoteRepository;
    @Mock private StockRepository stockRepository;
    @Mock private ServiceOrderExecutionRepository executionRepository;

    @InjectMocks
    private QuoteRepositoryImpl repository;

    @Nested
    @DisplayName("create")
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
    @DisplayName("update")
    class SaveUpdate {

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
                    .isInstanceOf(QuoteNotFoundException.class)
                    .hasMessageContaining(quoteId.toString());
        }

        @Test
        @DisplayName("Deve preservar o id do item de estoque inalterado, sem apagar e recriar a linha")
        void shouldPreserveStockItemIdWhenItemIsUnchanged() {
            var quoteId = UUID.randomUUID();
            var serviceOrderId = UUID.randomUUID();
            var existingItemId = UUID.randomUUID();

            var existingEntity = validEntity(quoteId, serviceOrderId);
            existingEntity.getStockItems().add(stockItemEntity(existingItemId, STOCK_ID, 1, existingEntity));

            var domainStockItem = QuoteStockItem.reconstruct(existingItemId, validStock(STOCK_ID),
                    new BigDecimal(PRICE_100), 5, LocalDateTime.now(), LocalDateTime.now());
            var quote = quoteWithStockItems(quoteId, serviceOrderId, List.of(domainStockItem));

            when(jpaQuoteRepository.findById(quoteId)).thenReturn(Optional.of(existingEntity));
            when(jpaQuoteRepository.save(any(QuoteEntity.class))).thenReturn(existingEntity);
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(validStock(STOCK_ID)));

            repository.save(quote);

            assertThat(existingEntity.getStockItems()).hasSize(1);
            assertThat(existingEntity.getStockItems().getFirst().getId()).isEqualTo(existingItemId);
            assertThat(existingEntity.getStockItems().getFirst().getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("Deve adicionar um novo item de estoque sem afetar o id do item existente")
        void shouldAddNewStockItemWithoutAffectingExistingId() {
            var quoteId = UUID.randomUUID();
            var serviceOrderId = UUID.randomUUID();
            var existingItemId = UUID.randomUUID();

            var existingEntity = validEntity(quoteId, serviceOrderId);
            existingEntity.getStockItems().add(stockItemEntity(existingItemId, STOCK_ID, 1, existingEntity));

            var unchangedItem = QuoteStockItem.reconstruct(existingItemId, validStock(STOCK_ID),
                    new BigDecimal(PRICE_100), 1, LocalDateTime.now(), LocalDateTime.now());
            var newItem = QuoteStockItem.create(validStock(STOCK_ID_2), 2);
            var quote = quoteWithStockItems(quoteId, serviceOrderId, List.of(unchangedItem, newItem));

            when(jpaQuoteRepository.findById(quoteId)).thenReturn(Optional.of(existingEntity));
            when(jpaQuoteRepository.save(any(QuoteEntity.class))).thenReturn(existingEntity);
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(validStock(STOCK_ID)));
            when(stockRepository.findById(STOCK_ID_2)).thenReturn(Optional.of(validStock(STOCK_ID_2)));

            repository.save(quote);

            assertThat(existingEntity.getStockItems()).hasSize(2);
            assertThat(existingEntity.getStockItems())
                    .anyMatch(i -> i.getId().equals(existingItemId));
        }

        @Test
        @DisplayName("Deve remover da entity o item de estoque que não está mais no domínio")
        void shouldRemoveStockItemNoLongerPresentInDomain() {
            var quoteId = UUID.randomUUID();
            var serviceOrderId = UUID.randomUUID();
            var keptItemId = UUID.randomUUID();
            var removedItemId = UUID.randomUUID();

            var existingEntity = validEntity(quoteId, serviceOrderId);
            existingEntity.getStockItems().add(stockItemEntity(keptItemId, STOCK_ID, 1, existingEntity));
            existingEntity.getStockItems().add(stockItemEntity(removedItemId, STOCK_ID_2, 2, existingEntity));

            var keptItem = QuoteStockItem.reconstruct(keptItemId, validStock(STOCK_ID),
                    new BigDecimal(PRICE_100), 1, LocalDateTime.now(), LocalDateTime.now());
            var quote = quoteWithStockItems(quoteId, serviceOrderId, List.of(keptItem));

            when(jpaQuoteRepository.findById(quoteId)).thenReturn(Optional.of(existingEntity));
            when(jpaQuoteRepository.save(any(QuoteEntity.class))).thenReturn(existingEntity);
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(validStock(STOCK_ID)));

            repository.save(quote);

            assertThat(existingEntity.getStockItems()).hasSize(1);
            assertThat(existingEntity.getStockItems().getFirst().getId()).isEqualTo(keptItemId);
        }

        @Test
        @DisplayName("Deve preservar o id do item de serviço inalterado")
        void shouldPreserveServiceItemIdWhenItemIsUnchanged() {
            var quoteId = UUID.randomUUID();
            var serviceOrderId = UUID.randomUUID();
            var existingItemId = UUID.randomUUID();
            var executionId = UUID.randomUUID();

            var existingEntity = validEntity(quoteId, serviceOrderId);
            existingEntity.getServiceItems().add(serviceItemEntity(existingItemId, executionId, existingEntity));

            var execution = validExecution(executionId, serviceOrderId);
            var domainServiceItem = QuoteServiceItem.reconstruct(existingItemId, execution,
                    new BigDecimal(PRICE_150), LocalDateTime.now(), LocalDateTime.now());
            var quote = quoteWithServiceItems(quoteId, serviceOrderId, List.of(domainServiceItem));

            when(jpaQuoteRepository.findById(quoteId)).thenReturn(Optional.of(existingEntity));
            when(jpaQuoteRepository.save(any(QuoteEntity.class))).thenReturn(existingEntity);
            when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

            repository.save(quote);

            assertThat(existingEntity.getServiceItems()).hasSize(1);
            assertThat(existingEntity.getServiceItems().getFirst().getId()).isEqualTo(existingItemId);
        }

        @Test
        @DisplayName("Deve adicionar um novo item de serviço sem afetar o id do item existente")
        void shouldAddNewServiceItemWithoutAffectingExistingId() {
            var quoteId = UUID.randomUUID();
            var serviceOrderId = UUID.randomUUID();
            var existingItemId = UUID.randomUUID();
            var executionId = UUID.randomUUID();
            var newExecutionId = UUID.randomUUID();

            var existingEntity = validEntity(quoteId, serviceOrderId);
            existingEntity.getServiceItems().add(serviceItemEntity(existingItemId, executionId, existingEntity));

            var existingExecution = validExecution(executionId, serviceOrderId);
            var unchangedItem = QuoteServiceItem.reconstruct(existingItemId, existingExecution,
                    new BigDecimal(PRICE_150), LocalDateTime.now(), LocalDateTime.now());

            var newExecution = validExecution(newExecutionId, serviceOrderId);
            var newItem = QuoteServiceItem.create(newExecution, new BigDecimal(PRICE_150));

            var quote = quoteWithServiceItems(quoteId, serviceOrderId, List.of(unchangedItem, newItem));

            when(jpaQuoteRepository.findById(quoteId)).thenReturn(Optional.of(existingEntity));
            when(jpaQuoteRepository.save(any(QuoteEntity.class))).thenReturn(existingEntity);
            when(executionRepository.findById(executionId)).thenReturn(Optional.of(existingExecution));
            when(executionRepository.findById(newExecutionId)).thenReturn(Optional.of(newExecution));

            repository.save(quote);

            assertThat(existingEntity.getServiceItems()).hasSize(2);
            assertThat(existingEntity.getServiceItems())
                    .anyMatch(i -> i.getId().equals(existingItemId));
        }

        @Test
        @DisplayName("Deve remover da entity o item de serviço que não está mais no domínio")
        void shouldRemoveServiceItemNoLongerPresentInDomain() {
            var quoteId = UUID.randomUUID();
            var serviceOrderId = UUID.randomUUID();
            var keptItemId = UUID.randomUUID();
            var removedItemId = UUID.randomUUID();
            var keptExecutionId = UUID.randomUUID();
            var removedExecutionId = UUID.randomUUID();

            var existingEntity = validEntity(quoteId, serviceOrderId);
            existingEntity.getServiceItems().add(serviceItemEntity(keptItemId, keptExecutionId, existingEntity));
            existingEntity.getServiceItems().add(serviceItemEntity(removedItemId, removedExecutionId, existingEntity));

            var keptExecution = validExecution(keptExecutionId, serviceOrderId);
            var keptItem = QuoteServiceItem.reconstruct(keptItemId, keptExecution,
                    new BigDecimal(PRICE_150), LocalDateTime.now(), LocalDateTime.now());

            var quote = quoteWithServiceItems(quoteId, serviceOrderId, List.of(keptItem));

            when(jpaQuoteRepository.findById(quoteId)).thenReturn(Optional.of(existingEntity));
            when(jpaQuoteRepository.save(any(QuoteEntity.class))).thenReturn(existingEntity);
            when(executionRepository.findById(keptExecutionId)).thenReturn(Optional.of(keptExecution));

            repository.save(quote);

            assertThat(existingEntity.getServiceItems()).hasSize(1);
            assertThat(existingEntity.getServiceItems().getFirst().getId()).isEqualTo(keptItemId);
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
            assertThat(result.getFirst().getServiceOrderId()).isEqualTo(serviceOrderId);
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
                new BigDecimal(PRICE_100), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Quote existingQuote(UUID id, UUID serviceOrderId) {
        return Quote.reconstruct(id, serviceOrderId, QuoteStatus.PENDING,
                new BigDecimal(PRICE_100), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Quote existingApprovedQuote(UUID id, UUID serviceOrderId) {
        var quote = Quote.reconstruct(id, serviceOrderId, QuoteStatus.PENDING,
                new BigDecimal("200.00"), null, List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now());
        quote.approve();
        return quote;
    }

    private Quote quoteWithStockItems(UUID id, UUID serviceOrderId, List<QuoteStockItem> stockItems) {
        return Quote.reconstruct(id, serviceOrderId, QuoteStatus.PENDING,
                new BigDecimal(PRICE_100), null, stockItems, List.of(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Quote quoteWithServiceItems(UUID id, UUID serviceOrderId, List<QuoteServiceItem> serviceItems) {
        return Quote.reconstruct(id, serviceOrderId, QuoteStatus.PENDING,
                new BigDecimal(PRICE_100), null, List.of(), serviceItems,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Stock validStock(UUID stockId) {
        return Stock.reconstruct(stockId, "Filtro de óleo", "Filtro",
                10, new BigDecimal(PRICE_100), "Filtros", 2,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private ServiceOrderExecution validExecution(UUID id, UUID serviceOrderId) {
        return ServiceOrderExecution.reconstruct(id, UUID.randomUUID(), serviceOrderId,
                ServiceOrderExecutionStatus.PENDING, LocalDateTime.now(), LocalDateTime.now(), null, null);
    }

    private QuoteEntity validEntity(UUID id, UUID serviceOrderId) {
        return QuoteEntity.builder()
                .id(id)
                .serviceOrderId(serviceOrderId)
                .status(QuoteStatus.PENDING)
                .totalPrice(new BigDecimal(PRICE_100))
                .stockItems(new ArrayList<>())
                .serviceItems(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private QuoteStockItemEntity stockItemEntity(UUID id, UUID stockId, int quantity, QuoteEntity quoteEntity) {
        return QuoteStockItemEntity.builder()
                .id(id)
                .quote(quoteEntity)
                .stockId(stockId)
                .unitPrice(new BigDecimal(PRICE_100))
                .quantity(quantity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private QuoteServiceItemEntity serviceItemEntity(UUID id, UUID executionId, QuoteEntity quoteEntity) {
        return QuoteServiceItemEntity.builder()
                .id(id)
                .quote(quoteEntity)
                .serviceOrderExecutionId(executionId)
                .price(new BigDecimal(PRICE_150))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}