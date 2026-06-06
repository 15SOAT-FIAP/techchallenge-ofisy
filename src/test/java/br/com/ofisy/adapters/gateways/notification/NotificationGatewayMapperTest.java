package br.com.ofisy.adapters.gateways.notification;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import br.com.ofisy.domain.notification.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationGatewayMapperTest {

    @Nested
    class ToEntity {
        @Test
        @DisplayName("Deve mapear todos os campos do domínio para a entidade JPA")
        void shouldMapAllFieldsFromDomainToEntity() {
            UUID id = UUID.randomUUID();
            UUID stockId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            Notification notification = Notification.builder()
                    .id(id)
                    .type(NotificationType.LOW_STOCK)
                    .stockId(stockId)
                    .message(NotificationMessage.fromString("Estoque baixo"))
                    .read(true)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            NotificationEntity entity = NotificationMapper.toEntity(notification);

            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getType()).isEqualTo(NotificationType.LOW_STOCK);
            assertThat(entity.getStockId()).isEqualTo(stockId);
            assertThat(entity.getQuoteId()).isNull();
            assertThat(entity.getMessage()).isEqualTo("Estoque baixo");
            assertThat(entity.getRead()).isTrue();
            assertThat(entity.getCreatedAt()).isEqualTo(now);
            assertThat(entity.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Deve retornar null se a notificação do domínio for nula")
        void shouldReturnNullWhenDomainIsNull() {
            assertThat(NotificationMapper.toEntity(null)).isNull();
        }
    }

    @Nested
    class ToDomain {
        @Test
        @DisplayName("Deve mapear todos os campos da entidade JPA para o domínio")
        void shouldMapAllFieldsFromEntityToDomain() {
            UUID id = UUID.randomUUID();
            UUID quoteId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            NotificationEntity entity = NotificationEntity.builder()
                    .id(id)
                    .type(NotificationType.QUOTE_GENERATED)
                    .quoteId(quoteId)
                    .message("Novo orçamento")
                    .read(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            Notification domain = NotificationMapper.toDomain(entity);

            assertThat(domain).isNotNull();
            assertThat(domain.getId()).isEqualTo(id);
            assertThat(domain.getType()).isEqualTo(NotificationType.QUOTE_GENERATED);
            assertThat(domain.getQuoteId()).isEqualTo(quoteId);
            assertThat(domain.getStockId()).isNull();
            assertThat(domain.getMessage().getContent()).isEqualTo("Novo orçamento");
            assertThat(domain.isRead()).isFalse();
            assertThat(domain.getCreatedAt()).isEqualTo(now);
            assertThat(domain.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Deve retornar null se a entidade JPA for nula")
        void shouldReturnNullWhenEntityIsNull() {
            assertThat(NotificationMapper.toDomain(null)).isNull();
        }
    }
}
