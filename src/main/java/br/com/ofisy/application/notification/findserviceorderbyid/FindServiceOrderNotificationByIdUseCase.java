package br.com.ofisy.application.notification.findserviceorderbyid;

import br.com.ofisy.domain.notification.Notification;
import java.util.UUID;

public interface FindServiceOrderNotificationByIdUseCase {
    Notification execute(UUID id);
}
