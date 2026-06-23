package br.com.ofisy.domain.serviceorderexecution;

import br.com.ofisy.application.serviceorderexecution.exceptions.InvalidServiceOrderExecutionStatusException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public enum ServiceOrderExecutionStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    private static final Map<ServiceOrderExecutionStatus, Set<ServiceOrderExecutionStatus>> ALLOWED =
            new EnumMap<>(ServiceOrderExecutionStatus.class);

    static {
        ALLOWED.put(PENDING, Set.of(IN_PROGRESS, CANCELLED));
        ALLOWED.put(IN_PROGRESS, Set.of(COMPLETED, CANCELLED));
        ALLOWED.put(COMPLETED, Set.of());
        ALLOWED.put(CANCELLED, Set.of());
    }

    public boolean canTransitionTo(ServiceOrderExecutionStatus next) {
        return ALLOWED.getOrDefault(this, Set.of()).contains(next);
    }

    public static ServiceOrderExecutionStatus from(String status) {
        try {
            return valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidServiceOrderExecutionStatusException(status);
        }
    }
}