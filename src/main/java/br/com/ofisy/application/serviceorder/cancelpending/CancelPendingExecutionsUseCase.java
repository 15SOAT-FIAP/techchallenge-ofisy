package br.com.ofisy.application.serviceorder.cancelpending;

import java.util.UUID;

public interface CancelPendingExecutionsUseCase {

    void execute(UUID serviceOrderId);
}