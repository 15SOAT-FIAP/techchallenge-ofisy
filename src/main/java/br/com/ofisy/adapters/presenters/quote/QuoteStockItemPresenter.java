package br.com.ofisy.adapters.presenters.quote;

import br.com.ofisy.adapters.controllers.quote.QuoteStockItemResponseDTO;
import br.com.ofisy.domain.quote.QuoteStockItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteStockItemPresenter {

    public static QuoteStockItemResponseDTO present(QuoteStockItem item) {
        return new QuoteStockItemResponseDTO(
                item.getId(),
                item.getStock().getId(),
                item.getStock().getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                item.getCreatedAt()
        );
    }
}
