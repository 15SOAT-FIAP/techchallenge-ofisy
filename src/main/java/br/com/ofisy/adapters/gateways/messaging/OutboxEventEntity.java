package br.com.ofisy.adapters.gateways.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OutboxEventEntity implements Persistable<UUID> {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private UUID serviceOrderId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private Instant occurredAt;

    private Instant publishedAt;

    // Contexto do trace da requisição que gerou o evento, restaurado pelo publisher para o fluxo seguir no mesmo trace
    @JdbcTypeCode(SqlTypes.JSON)
    private String traceContext;

    @Override
    public UUID getId() {
        return eventId;
    }

    @Override
    public boolean isNew() {
        return true;
    }

    public void markPublished(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}