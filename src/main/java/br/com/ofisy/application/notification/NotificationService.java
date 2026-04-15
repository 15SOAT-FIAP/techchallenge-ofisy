package br.com.ofisy.application.notification;

import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.domain.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String STOCK_ALERT_MESSAGE = "Estoque notificado para compras de insumos";

    private final NotificationRepository notificationRepository;
    //stockRepository;
    private final MockNotificationSender mockNotificationSender;


    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> listUnread() {
        return notificationRepository.findAllUnread()
                .stream()
                .map(NotificationMapper::toDTO)
                .toList();
    }
}