package br.com.ofisy.application.notification.usecases;

import br.com.ofisy.application.notification.ports.in.NotificationResponse;

import java.util.List;

public interface FindAllNotificationsUseCase {
    List<NotificationResponse> execute();
}
