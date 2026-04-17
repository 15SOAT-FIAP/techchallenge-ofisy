package br.com.ofisy.application.notification;

import br.com.ofisy.application.notification.dto.NotificationRequestDTO;
import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StockRepository stockRepository;
    private final MockNotificationSender mockNotificationSender;


    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> listAll() {
        return notificationRepository.findAll()
                .stream()
                .map(NotificationMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> listUnread() {
        return notificationRepository.findAllUnread()
                .stream()
                .map(NotificationMapper::toDTO)
                .toList();
    }

    @Transactional
    public NotificationResponseDTO markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        notification.markAsRead();
        Notification saved = notificationRepository.save(notification);
        return NotificationMapper.toDTO(saved);
    }

    @Transactional
    public void sendStockAlert(Stock stock) {
        String message = "Estoque baixo: " + stock.getProductName() +
                " atingiu o limite mínimo de " + stock.getMinThreshold() + " unidades";
        Notification notification = Notification.createStockAlert(stock.getId(), message);
        notificationRepository.save(notification);
        mockNotificationSender.send(notification.getMessage());
    }

    @Transactional
    public NotificationResponseDTO send(NotificationRequestDTO request) {
        stockRepository.findById(request.stockId())
                .orElseThrow(() -> new StockNotFoundException(request.stockId()));

        Notification notification = Notification.createStockAlert(request.stockId(), request.message());
        Notification saved = notificationRepository.save(notification);
        mockNotificationSender.send(saved.getMessage());
        return NotificationMapper.toDTO(saved);
    }

}