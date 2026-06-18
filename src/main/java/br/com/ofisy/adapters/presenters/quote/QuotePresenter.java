package br.com.ofisy.adapters.presenters.quote;

import br.com.ofisy.adapters.controllers.quote.dto.QuoteResponseDTO;
import br.com.ofisy.domain.quote.Quote;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuotePresenter {

    public static QuoteResponseDTO present(Quote quote) {
        return new QuoteResponseDTO(
                quote.getId(),
                quote.getServiceOrderId(),
                quote.getStatus(),
                quote.getTotalPrice(),
                quote.getQuoteRefusalReason(),
                quote.getStockItems().stream().map(QuoteStockItemPresenter::present).toList(),
                quote.getServiceItems().stream().map(QuoteServiceItemPresenter::present).toList(),
                quote.getCreatedAt(),
                quote.getUpdatedAt()
        );
    }
}
