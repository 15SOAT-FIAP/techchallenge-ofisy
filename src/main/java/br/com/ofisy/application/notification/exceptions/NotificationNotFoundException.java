package br.com.ofisy.application.notification.exceptions;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(UUID id) {
        super("Notificação não encontrada: " + id);
    }
}
