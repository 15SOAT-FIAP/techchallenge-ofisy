package br.com.ofisy.domain.serviceorder;

public enum ServiceOrderStatus {
    RECEIVED,
    IN_DIAGNOSTIC,
    AWAITING_APPROVAL,
    AWAITING_EXECUTION,
    IN_PROGRESS,
    FINISHED,
    DELIVERED,
    CANCELLED,
}