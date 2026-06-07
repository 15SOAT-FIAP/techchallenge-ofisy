package br.com.ofisy.application.notification.findunread;

import br.com.ofisy.domain.notification.Notification;
import java.util.List;

public interface FindUnreadNotificationsUseCase {
    List<Notification> execute();
}
