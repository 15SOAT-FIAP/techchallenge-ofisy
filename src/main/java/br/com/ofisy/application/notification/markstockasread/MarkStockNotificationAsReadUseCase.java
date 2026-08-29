package br.com.ofisy.application.notification.markstockasread;

import br.com.ofisy.domain.notification.Notification;
import java.util.UUID;

public interface MarkStockNotificationAsReadUseCase {

    Notification execute(MarkAsReadCommand command);

    record MarkAsReadCommand(UUID id) {}
}
