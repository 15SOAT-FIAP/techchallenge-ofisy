package br.com.ofisy.application.notification.usecases;

import br.com.ofisy.application.notification.ports.in.MarkAsReadCommand;
import br.com.ofisy.application.notification.ports.in.NotificationResponse;

public interface MarkNotificationAsReadUseCase {
    NotificationResponse execute(MarkAsReadCommand command);
}
