package br.com.ofisy.domain.messaging;

public interface OutboxEventRepository {

    void save(OutboxEvent outboxEvent);
}