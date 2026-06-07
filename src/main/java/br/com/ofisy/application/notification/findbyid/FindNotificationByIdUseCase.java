package br.com.ofisy.application.notification.findbyid;

import br.com.ofisy.domain.notification.Notification;
import java.util.UUID;

public interface FindNotificationByIdUseCase {
    Notification execute(UUID id);
}
