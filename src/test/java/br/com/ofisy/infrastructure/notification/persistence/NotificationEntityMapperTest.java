package br.com.ofisy.infrastructure.notification.persistence;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import br.com.ofisy.domain.notification.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEntityMapperTest {

    private NotificationEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NotificationEntityMapper();
    }

    @Test
    @DisplayName("Deve converter Notification para NotificationJpaEntity")
    void shouldConvertDomainToEntity() {
        UUID stockId = UUID.randomUUID();
        NotificationMessage message = NotificationMessage.forLowStock("Radiador", 2, 5);
        Notification notification = Notification.createForStock(stockId, message);

        NotificationJpaEntity entity = mapper.toEntity(notification);

        assertThat(entity.getId()).isEqualTo(notification.getId());
        assertThat(entity.getType()).isEqualTo(NotificationType.LOW_STOCK);
        assertThat(entity.getStockId()).isEqualTo(stockId);
        assertThat(entity.getQuoteId()).isNull();
        assertThat(entity.getMessage()).isEqualTo(message.getContent());
        assertThat(entity.getRead()).isFalse();
        assertThat(entity.getCreatedAt()).isEqualTo(notification.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(notification.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve converter NotificationJpaEntity para Notification usando reconstitute")
    void shouldConvertEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID stockId = UUID.randomUUID();
        String messageContent = "Estoque baixo para Radiador. Quantidade atual: 2. Mínimo: 5";
        LocalDateTime now = LocalDateTime.now();

        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(id);
        entity.setType(NotificationType.LOW_STOCK);
        entity.setStockId(stockId);
        entity.setQuoteId(null);
        entity.setMessage(messageContent);
        entity.setRead(false);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        Notification notification = mapper.toDomain(entity);

        assertThat(notification.getId()).isEqualTo(id);
        assertThat(notification.getType()).isEqualTo(NotificationType.LOW_STOCK);
        assertThat(notification.getStockId()).isEqualTo(stockId);
        assertThat(notification.getQuoteId()).isNull();
        assertThat(notification.getMessage().getContent()).isEqualTo(messageContent);
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getCreatedAt()).isEqualTo(now);
        assertThat(notification.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Deve preservar estado de leitura ao converter")
    void shouldPreserveReadStateWhenConverting() {
        NotificationMessage message = NotificationMessage.fromString("Test message");
        Notification notification = Notification.createForStock(UUID.randomUUID(), message);
        notification.markAsRead();

        NotificationJpaEntity entity = mapper.toEntity(notification);

        assertThat(entity.getRead()).isTrue();
    }

    @Test
    @DisplayName("Deve fazer conversão ida e volta sem perda de dados")
    void shouldConvertBackAndForthWithoutDataLoss() {
        UUID stockId = UUID.randomUUID();
        NotificationMessage message = NotificationMessage.forLowStock("Radiador", 2, 5);
        Notification original = Notification.createForStock(stockId, message);

        NotificationJpaEntity entity = mapper.toEntity(original);
        Notification restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getType()).isEqualTo(original.getType());
        assertThat(restored.getStockId()).isEqualTo(original.getStockId());
        assertThat(restored.getMessage().getContent()).isEqualTo(original.getMessage().getContent());
        assertThat(restored.isRead()).isEqualTo(original.isRead());
    }
}
