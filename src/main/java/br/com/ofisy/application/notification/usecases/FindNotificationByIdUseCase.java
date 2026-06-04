package br.com.ofisy.application.notification.usecases;

import br.com.ofisy.application.notification.ports.in.NotificationResponse;

import java.util.UUID;

public interface FindNotificationByIdUseCase {
    NotificationResponse execute(UUID id);
}
