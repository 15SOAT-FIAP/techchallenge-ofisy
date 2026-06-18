package br.com.ofisy.adapters.presenters.quote;

import br.com.ofisy.adapters.controllers.quote.dto.QuoteServiceItemResponseDTO;
import br.com.ofisy.domain.quote.QuoteServiceItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteServiceItemPresenter {

    public static QuoteServiceItemResponseDTO present(QuoteServiceItem item) {
        return new QuoteServiceItemResponseDTO(
                item.getId(),
                item.getServiceOrderExecution().getId(),
                item.getPrice(),
                item.getCreatedAt()
        );
    }
}
