package br.com.ofisy.application.notification.markasread;

import br.com.ofisy.domain.notification.Notification;
import java.util.UUID;

public interface MarkNotificationAsReadUseCase {

    Notification execute(MarkAsReadCommand command);

    record MarkAsReadCommand(UUID id) {}
}
