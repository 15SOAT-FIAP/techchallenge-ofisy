package br.com.ofisy.application.notification.findstockbyid;

import br.com.ofisy.domain.notification.Notification;
import java.util.UUID;

public interface FindStockNotificationByIdUseCase {
    Notification execute(UUID id);
}
