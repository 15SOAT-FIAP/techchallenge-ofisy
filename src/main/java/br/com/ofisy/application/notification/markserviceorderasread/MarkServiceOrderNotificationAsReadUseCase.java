package br.com.ofisy.application.notification.markserviceorderasread;

import br.com.ofisy.domain.notification.Notification;
import java.util.UUID;

public interface MarkServiceOrderNotificationAsReadUseCase {

    Notification execute(MarkAsReadCommand command);

    record MarkAsReadCommand(UUID id) {}
}
