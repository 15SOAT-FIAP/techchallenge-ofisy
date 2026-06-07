package br.com.ofisy.domain.notification;

import br.com.ofisy.domain.notification.exceptions.InvalidNotificationMessageException;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class NotificationMessage {

    private final String content;

    private NotificationMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidNotificationMessageException("Mensagem não pode ser vazia");
        }
        if (content.length() > 255) {
            throw new InvalidNotificationMessageException("Mensagem não pode ter mais de 255 caracteres");
        }
        this.content = content;
    }

    public static NotificationMessage forLowStock(String productName, int currentQuantity, int minThreshold) {
        String content = String.format(
                "Estoque baixo para %s. Quantidade atual: %d. Mínimo: %d",
                productName, currentQuantity, minThreshold
        );
        return new NotificationMessage(content);
    }

    public static NotificationMessage forQuote(UUID quoteId, UUID serviceOrderId, BigDecimal totalPrice) {
        String content = String.format(
                Locale.US,
                "Orçamento #%s gerado para a ordem de serviço '%s'. Valor total: R$ %.2f",
                quoteId, serviceOrderId, totalPrice
        );
        return new NotificationMessage(content);
    }

    public static NotificationMessage fromString(String content) {
        return new NotificationMessage(content);
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationMessage that = (NotificationMessage) o;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    @Override
    public String toString() {
        return content;
    }
}
