package br.com.ofisy.application.notification.usecases;

import br.com.ofisy.application.notification.ports.in.CreateQuoteCommand;
import br.com.ofisy.application.notification.ports.in.NotificationResponse;

public interface CreateQuoteNotificationUseCase {
    NotificationResponse execute(CreateQuoteCommand command);
}
