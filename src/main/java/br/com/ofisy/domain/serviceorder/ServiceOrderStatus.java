package br.com.ofisy.domain.serviceorder;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public enum ServiceOrderStatus {

    RECEIVED(5),
    IN_DIAGNOSTIC(4),
    AWAITING_APPROVAL(3),
    AWAITING_EXECUTION(2),
    IN_PROGRESS(1),
    FINISHED(0),
    DELIVERED(0),
    CANCELLED(0);

    public final Integer priority;

    ServiceOrderStatus(Integer priority) {
        this.priority = priority;
    }

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