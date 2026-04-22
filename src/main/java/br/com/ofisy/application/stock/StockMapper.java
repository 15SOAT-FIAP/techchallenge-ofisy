package br.com.ofisy.application.stock;

import br.com.ofisy.application.stock.dto.CreateStockRequestDTO;
import br.com.ofisy.application.stock.dto.StockResponseDTO;
import br.com.ofisy.domain.stock.Stock;

public class StockMapper {

    public static Stock toDomain(CreateStockRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("CreateStockRequestDTO não pode ser nulo");
        }

        return Stock.create(
                request.productName(),
                request.description(),
                request.quantity(),
                request.unitPrice(),
                request.category(),
                request.minThreshold()
        );
    }

    public static StockResponseDTO toDTO(Stock stock) {
        if (stock == null) {
            throw new IllegalArgumentException("Stock não pode ser nulo");
        }

        return new StockResponseDTO(
                stock.getId(),
                stock.getProductName(),
                stock.getDescription(),
                stock.getQuantity(),
                stock.getUnitPrice(),
                stock.getCategory(),
                stock.getCreatedAt(),
                stock.getUpdatedAt(),
                stock.getMinThreshold()
        );
    }
}
