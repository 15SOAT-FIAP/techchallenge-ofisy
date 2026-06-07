package br.com.ofisy.application.notification.createquote;

import br.com.ofisy.domain.notification.Notification;
import java.math.BigDecimal;
import java.util.UUID;

public interface CreateQuoteNotificationUseCase {

    Notification execute(CreateQuoteCommand command);

    record CreateQuoteCommand(
            UUID quoteId,
            UUID serviceOrderId,
            BigDecimal totalPrice
    ) {}
}
