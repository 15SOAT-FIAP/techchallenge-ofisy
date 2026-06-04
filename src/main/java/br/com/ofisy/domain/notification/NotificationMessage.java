package br.com.ofisy.domain.notification;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class NotificationMessage {
    private final String content;

    private NotificationMessage(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Mensagem não pode ser vazia");
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

    public static NotificationMessage forQuote(UUID quoteId, UUID serviceOrderId, java.math.BigDecimal totalPrice) {
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
        return "NotificationMessage{" + content + '}';
    }
}
