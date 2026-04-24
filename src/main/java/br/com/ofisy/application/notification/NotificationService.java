package br.com.ofisy.application.notification;

import br.com.ofisy.application.notification.dto.CreateNotificationRequestDTO;
import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponseDTO create(CreateNotificationRequestDTO request) {
        Notification notification = Notification.create(
                request.type(),
                request.stockId(),
                request.message()
        );
        Notification saved = notificationRepository.save(notification);
        return toResponseDTO(saved);
    }

    @Transactional
    public NotificationResponseDTO createLowStockNotification(UUID stockId, String productName, Integer currentQuantity, Integer minThreshold) {
        String message = String.format(
                "Estoque baixo para %s. Quantidade atual: %d. Mínimo: %d",
                productName,
                currentQuantity,
                minThreshold
        );
        Notification notification = Notification.create("LOW_STOCK", stockId, message);
        Notification saved = notificationRepository.save(notification);
        return toResponseDTO(saved);
    }

    @Transactional
    public NotificationResponseDTO createBudgetNotification(String budgetNumber, String customerName, Double totalValue) {
        String message = String.format(
                "Orçamento #%s gerado para o cliente '%s'. Valor total: R$ %.2f",
                budgetNumber,
                customerName,
                totalValue
        );
        Notification notification = Notification.create("ORCAMENTO_GERADO", null, message);
        Notification saved = notificationRepository.save(notification);
        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> findAll() {
        return notificationRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> findUnread() {
        return notificationRepository.findByRead(false).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationResponseDTO markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        notification.markAsRead();
        return toResponseDTO(notification);
    }

    private NotificationResponseDTO toResponseDTO(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getType(),
                notification.getStockId(),
                notification.getMessage(),
                notification.getRead(),
                notification.getCreatedAt()
        );
    }
}
