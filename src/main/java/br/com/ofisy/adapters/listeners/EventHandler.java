package br.com.ofisy.adapters.listeners;

import br.com.ofisy.adapters.listeners.dto.EventEnvelope;

public interface EventHandler {

    String type();

    void handle(EventEnvelope envelope);
}