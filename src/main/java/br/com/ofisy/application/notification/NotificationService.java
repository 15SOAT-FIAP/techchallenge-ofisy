package br.com.ofisy.application.notification;

import br.com.ofisy.application.notification.dto.CreateNotificationRequestDTO;
import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponseDTO create(CreateNotificationRequestDTO request) {
        Notification notification = NotificationMapper.toDomain(request);
        Notification saved = notificationRepository.save(notification);
        return NotificationMapper.toDTO(saved);
    }

    @Transactional
    public NotificationResponseDTO createLowStockNotification(Stock stock) {
        String message = String.format(
                "Estoque baixo para %s. Quantidade atual: %d. Mínimo: %d",
                stock.getProductName(),
                stock.getQuantity(),
                stock.getMinThreshold()
        );
        Notification notification = Notification.create("LOW_STOCK", stock.getId(), message);
        Notification saved = notificationRepository.save(notification);
        return NotificationMapper.toDTO(saved);
    }

    @Transactional
    public NotificationResponseDTO createQuoteNotification(String quoteNumber, String customerName, Double totalValue) {
        String message = String.format(
                "Orçamento #%s gerado para o cliente '%s'. Valor total: R$ %.2f",
                quoteNumber,
                customerName,
                totalValue
        );
        Notification notification = Notification.create("QUOTE_GENERATED", null, message);
        Notification saved = notificationRepository.save(notification);
        return NotificationMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> findAll() {
        return notificationRepository.findAll().stream()
                .map(NotificationMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> findUnread() {
        return notificationRepository.findByRead(false).stream()
                .map(NotificationMapper::toDTO)
                .toList();
    }

    @Transactional
    public NotificationResponseDTO markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        notification.markAsRead();
        return NotificationMapper.toDTO(notification);
    }
}
