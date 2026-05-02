package br.com.ofisy.application.quote;

import br.com.ofisy.application.quote.dto.*;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.application.stock.StockService;
import br.com.ofisy.domain.quote.*;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final StockService stockService;
    private final StockRepository stockRepository;
    private final ServiceOrderExecutionRepository serviceOrderExecutionRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final QuoteMapper mapper;

    @Transactional
    public QuoteResponseDTO create(CreateQuoteRequestDTO request) {
        List<QuoteStockItem> stockItems = buildStockItems(request.stockItems());
        List<QuoteServiceItem> serviceItems = buildServiceItems(
                request.serviceItems() != null ? request.serviceItems() : List.of()
        );

        Quote quote = Quote.create(request.serviceOrderId(), stockItems, serviceItems);
        return mapper.toResponse(quoteRepository.save(quote));
    }

    @Transactional
    public QuoteResponseDTO approve(UUID id) {
        Quote quote = findQuoteById(id);
        quote.approve();
        return mapper.toResponse(quoteRepository.save(quote));
    }

    @Transactional
    public QuoteResponseDTO reprove(UUID id, ReproveQuoteRequestDTO request) {
        Quote quote = findQuoteById(id);
        quote.reprove(request.reason());
        return mapper.toResponse(quoteRepository.save(quote));
    }

    @Transactional(readOnly = true)
    public QuoteResponseDTO findById(UUID id) {
        return mapper.toResponse(findQuoteById(id));
    }

    @Transactional(readOnly = true)
    public List<QuoteResponseDTO> findByServiceOrderId(UUID serviceOrderId) {
        return quoteRepository.findByServiceOrderId(serviceOrderId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    private Quote findQuoteById(UUID id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new QuoteNotFoundException(id));
    }

    private Stock findStockById(UUID id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estoque com id " + id + " não encontrado"));
    }

    private List<QuoteStockItem> buildStockItems(List<StockItemRequestDTO> requests) {
        List<QuoteStockItem> items = new ArrayList<>();

        for (StockItemRequestDTO request : requests) {
            Stock stock = findStockById(request.stockId());

            boolean duplicate = items.stream()
                    .anyMatch(i -> i.getStock().getId().equals(request.stockId()));
            if (duplicate) {
                throw new QuoteItemAlreadyExistsException(stock.getProductName());
            }

            stockService.consumeStock(request.stockId(), request.quantity());

            items.add(QuoteStockItem.create(stock, request.quantity()));
        }

        return items;
    }

    private List<QuoteServiceItem> buildServiceItems(List<ServiceItemRequestDTO> requests) {
        List<QuoteServiceItem> items = new ArrayList<>();

        for (ServiceItemRequestDTO request : requests) {
            ServiceOrderExecution serviceOrderExecution = findServiceOrderExecutionById(
                    request.serviceOrderExecutionId()
            );

            boolean duplicate = items.stream()
                    .anyMatch(i -> i.getServiceOrderExecution().getId().equals(request.serviceOrderExecutionId()));
            if (duplicate) {
                throw new QuoteItemAlreadyExistsException(
                        "Serviço " + request.serviceOrderExecutionId()
                );
            }

            ServiceCatalog service = findServiceById(serviceOrderExecution.getServiceCatalogId());
            items.add(QuoteServiceItem.create(serviceOrderExecution, service.getPrice()));
        }

        return items;
    }

    private ServiceOrderExecution findServiceOrderExecutionById(UUID id) {
        return serviceOrderExecutionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Execução de serviço com id " + id + " não encontrada"));
    }

    private ServiceCatalog findServiceById(UUID id) {
        return serviceCatalogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Serviço com id " + id + " não encontrado"));
    }
}