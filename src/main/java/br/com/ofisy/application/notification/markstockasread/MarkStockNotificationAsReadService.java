package br.com.ofisy.application.notification.markstockasread;

import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarkStockNotificationAsReadService implements MarkStockNotificationAsReadUseCase {

    private final NotificationRepository notificationRepository;

    public MarkStockNotificationAsReadService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification execute(MarkAsReadCommand command) {
        Notification notification = notificationRepository.findById(command.id())
                .orElseThrow(() -> new NotificationNotFoundException(command.id()));

        // Mesmo raciocinio do findStockById: tipo errado vira "nao encontrada",
        // pra nao deixar quem tem acesso a estoque marcar (ou nem saber que
        // existe) uma notificacao de OS como lida.
        if (notification.getType() != NotificationType.LOW_STOCK) {
            throw new NotificationNotFoundException(command.id());
        }

        notification.markAsRead();
        return notificationRepository.save(notification);
    }
}
