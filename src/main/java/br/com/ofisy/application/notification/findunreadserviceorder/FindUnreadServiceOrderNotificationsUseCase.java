package br.com.ofisy.application.notification.findunreadserviceorder;

import br.com.ofisy.domain.notification.Notification;
import java.util.List;

public interface FindUnreadServiceOrderNotificationsUseCase {
    List<Notification> execute();
}
