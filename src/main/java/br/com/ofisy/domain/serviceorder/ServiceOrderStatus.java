package br.com.ofisy.domain.serviceorder;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public enum ServiceOrderStatus {
    RECEIVED,
    IN_DIAGNOSTIC,
    AWAITING_APPROVAL,
    AWAITING_EXECUTION,
    IN_PROGRESS,
    FINISHED,
    DELIVERED,
    CANCELLED;

    private static final Map<ServiceOrderStatus, Set<ServiceOrderStatus>> ALLOWED =
            new EnumMap<>(ServiceOrderStatus.class);

    static {
        ALLOWED.put(RECEIVED, Set.of(IN_DIAGNOSTIC, CANCELLED));
        ALLOWED.put(IN_DIAGNOSTIC, Set.of(AWAITING_APPROVAL, CANCELLED));
        ALLOWED.put(AWAITING_APPROVAL, Set.of(AWAITING_EXECUTION, CANCELLED));
        ALLOWED.put(AWAITING_EXECUTION, Set.of(IN_PROGRESS, CANCELLED));
        ALLOWED.put(IN_PROGRESS, Set.of(FINISHED, CANCELLED));
        ALLOWED.put(FINISHED, Set.of(DELIVERED));
        ALLOWED.put(DELIVERED, Set.of());
        ALLOWED.put(CANCELLED, Set.of());
    }

    public boolean canTransitionTo(ServiceOrderStatus nextStatus) {
        return ALLOWED.get(this).contains(nextStatus);
    }
}