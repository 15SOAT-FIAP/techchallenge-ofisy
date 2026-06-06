package br.com.ofisy.application.notification.findall;

import br.com.ofisy.domain.notification.Notification;
import java.util.List;

public interface FindAllNotificationsUseCase {
    List<Notification> execute();
}
