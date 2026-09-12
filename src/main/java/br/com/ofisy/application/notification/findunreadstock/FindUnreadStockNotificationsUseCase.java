package br.com.ofisy.application.notification.findunreadstock;

import br.com.ofisy.domain.notification.Notification;
import java.util.List;

public interface FindUnreadStockNotificationsUseCase {
    List<Notification> execute();
}
