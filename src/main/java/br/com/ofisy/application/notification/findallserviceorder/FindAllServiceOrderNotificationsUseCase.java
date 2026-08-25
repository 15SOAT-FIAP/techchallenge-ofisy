package br.com.ofisy.application.notification.findallserviceorder;

import br.com.ofisy.domain.notification.Notification;
import java.util.List;

public interface FindAllServiceOrderNotificationsUseCase {
    List<Notification> execute();
}
