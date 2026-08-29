package br.com.ofisy.application.notification.findserviceorderbyid;

import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FindServiceOrderNotificationByIdService implements FindServiceOrderNotificationByIdUseCase {

    private final NotificationRepository notificationRepository;

    public FindServiceOrderNotificationByIdService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification execute(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));

        // Mesmo raciocinio do lado de estoque, espelhado: tipo errado vira
        // "nao encontrada", nao revela a existencia de notificacoes de
        // estoque pra quem so tem acesso as de OS.
        if (notification.getType() != NotificationType.QUOTE_GENERATED) {
            throw new NotificationNotFoundException(id);
        }

        return notification;
    }
}
