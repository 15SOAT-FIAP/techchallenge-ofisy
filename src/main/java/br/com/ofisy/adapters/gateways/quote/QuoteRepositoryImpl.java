package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.stock.StockRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class QuoteRepositoryImpl implements QuoteRepository {

    private final JpaQuoteRepository jpa;
    private final StockRepository stockRepository;
    private final ServiceOrderExecutionRepository executionRepository;

    public QuoteRepositoryImpl(JpaQuoteRepository jpa,
                               StockRepository stockRepository,
                               ServiceOrderExecutionRepository executionRepository) {
        this.jpa = jpa;
        this.stockRepository = stockRepository;
        this.executionRepository = executionRepository;
    }

    @Override
    public Quote save(Quote quote) {
        if (quote.getId() == null) {
            QuoteEntity entity = QuoteMapper.toEntity(quote);
            return QuoteMapper.toDomain(jpa.save(entity), stockRepository, executionRepository);
        }
        return update(quote);
    }

    @Override
    public Optional<Quote> findById(UUID id) {
        return jpa.findById(id)
                .map(entity -> QuoteMapper.toDomain(entity, stockRepository, executionRepository));
    }

    @Override
    public List<Quote> findByServiceOrderId(UUID serviceOrderId) {
        return jpa.findByServiceOrderId(serviceOrderId).stream()
                .map(entity -> QuoteMapper.toDomain(entity, stockRepository, executionRepository))
                .toList();
    }

    @Override
    public boolean existsByServiceOrderId(UUID serviceOrderId) {
        return jpa.existsByServiceOrderId(serviceOrderId);
    }

    private Quote update(Quote quote) {
        QuoteEntity entity = jpa.findById(quote.getId()).orElseThrow(() -> new QuoteNotFoundException(quote.getId()));

        entity.update(quote.getStatus(), quote.getTotalPrice(),
                quote.getQuoteRefusalReason(), quote.getUpdatedAt());

        reconcileStockItems(entity, quote);
        reconcileServiceItems(entity, quote);

        return QuoteMapper.toDomain(jpa.save(entity), stockRepository, executionRepository);
    }

    private void reconcileStockItems(QuoteEntity entity, Quote quote) {
        Map<UUID, QuoteStockItemEntity> existingById = entity.getStockItems().stream()
                .filter(quoteStockItem -> quoteStockItem.getId() != null)
                .collect(Collectors.toMap(QuoteStockItemEntity::getId, Function.identity()));

        List<QuoteStockItemEntity> reconciled = new ArrayList<>();

        for (QuoteStockItem domainItem : quote.getStockItems()) {
            QuoteStockItemEntity existing = domainItem.getId() != null
                    ? existingById.get(domainItem.getId())
                    : null;

            if (existing != null) {
                existing.update(domainItem.getStock().getId(), domainItem.getUnitPrice(), domainItem.getQuantity(), domainItem.getUpdatedAt());
                reconciled.add(existing);
            } else {
                reconciled.add(QuoteStockItemMapper.toEntity(domainItem, entity));
            }
        }

        entity.getStockItems().clear();
        entity.getStockItems().addAll(reconciled);
    }

    private void reconcileServiceItems(QuoteEntity entity, Quote quote) {
        Map<UUID, QuoteServiceItemEntity> existingById = entity.getServiceItems().stream()
                .filter(quoteServiceItem -> quoteServiceItem.getId() != null)
                .collect(Collectors.toMap(QuoteServiceItemEntity::getId, Function.identity()));

        List<QuoteServiceItemEntity> reconciled = new ArrayList<>();

        for (QuoteServiceItem domainItem : quote.getServiceItems()) {
            QuoteServiceItemEntity existing = domainItem.getId() != null
                    ? existingById.get(domainItem.getId())
                    : null;

            if (existing != null) {
                existing.update(domainItem.getServiceOrderExecution().getId(), domainItem.getPrice(), domainItem.getUpdatedAt());
                reconciled.add(existing);
            } else {
                reconciled.add(QuoteServiceItemMapper.toEntity(domainItem, entity));
            }
        }

        entity.getServiceItems().clear();
        entity.getServiceItems().addAll(reconciled);
    }
}