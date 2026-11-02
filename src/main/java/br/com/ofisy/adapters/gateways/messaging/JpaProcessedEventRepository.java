package br.com.ofisy.adapters.gateways.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaProcessedEventRepository extends JpaRepository<ProcessedEventEntity, UUID> {
}