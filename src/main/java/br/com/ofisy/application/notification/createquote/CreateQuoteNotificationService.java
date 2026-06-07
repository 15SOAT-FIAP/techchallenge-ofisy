package br.com.ofisy.application.notification.createquote;

import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import br.com.ofisy.domain.notification.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateQuoteNotificationService implements CreateQuoteNotificationUseCase {

    private final NotificationRepository notificationRepository;

    public CreateQuoteNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification execute(CreateQuoteCommand command) {
        NotificationMessage message = NotificationMessage.forQuote(
                command.quoteId(),
                command.serviceOrderId(),
                command.totalPrice()
        );
        Notification notification = Notification.createForQuote(command.quoteId(), message);
        return notificationRepository.save(notification);
    }
}
