package br.com.ofisy.adapters.listeners.dto;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope(
        UUID eventId,
        String type,
        UUID serviceOrderId,
        Instant occurredAt,
        JsonNode payload
) {}