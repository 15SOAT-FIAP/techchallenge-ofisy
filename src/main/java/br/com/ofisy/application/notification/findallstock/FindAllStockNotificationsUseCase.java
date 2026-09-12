package br.com.ofisy.application.notification.findallstock;

import br.com.ofisy.domain.notification.Notification;
import java.util.List;

public interface FindAllStockNotificationsUseCase {
    List<Notification> execute();
}
