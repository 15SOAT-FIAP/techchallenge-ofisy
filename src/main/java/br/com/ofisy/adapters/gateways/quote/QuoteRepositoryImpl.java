package br.com.ofisy.adapters.gateways.quote;

import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.stock.StockRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        QuoteEntity entity = jpa.findById(quote.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quote não encontrado para atualização: " + quote.getId()));

        entity.update(quote.getStatus(), quote.getTotalPrice(),
                quote.getQuoteRefusalReason(), quote.getUpdatedAt());

        entity.getStockItems().clear();
        entity.getServiceItems().clear();
        jpa.flush();

        entity.getStockItems().addAll(
                quote.getStockItems().stream()
                        .map(item -> QuoteStockItemEntityMapper.toEntity(item, entity))
                        .toList());

        entity.getServiceItems().addAll(
                quote.getServiceItems().stream()
                        .map(item -> QuoteServiceItemEntityMapper.toEntity(item, entity))
                        .toList());

        return QuoteMapper.toDomain(jpa.save(entity), stockRepository, executionRepository);
    }
}