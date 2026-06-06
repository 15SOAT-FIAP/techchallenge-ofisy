package br.com.ofisy.application.notification.createlowstock;

import br.com.ofisy.domain.notification.Notification;
import java.util.UUID;

public interface CreateLowStockNotificationUseCase {

    Notification execute(CreateLowStockCommand command);

    record CreateLowStockCommand(
            UUID stockId,
            String productName,
            int currentQuantity,
            int minThreshold
    ) {}
}
