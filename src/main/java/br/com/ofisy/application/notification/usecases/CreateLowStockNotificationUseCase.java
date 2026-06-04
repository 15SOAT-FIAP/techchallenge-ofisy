package br.com.ofisy.application.notification.usecases;

import br.com.ofisy.application.notification.ports.in.CreateLowStockCommand;
import br.com.ofisy.application.notification.ports.in.NotificationResponse;

public interface CreateLowStockNotificationUseCase {
    NotificationResponse execute(CreateLowStockCommand command);
}
