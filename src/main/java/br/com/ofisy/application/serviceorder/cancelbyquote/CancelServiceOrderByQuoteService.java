package br.com.ofisy.application.serviceorder.cancelbyquote;

import br.com.ofisy.application.serviceorder.cancel.CancelServiceOrderService;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.config.metrics.ServiceOrderMetrics;
import br.com.ofisy.domain.messaging.OutboxEvent;
import br.com.ofisy.domain.messaging.OutboxEventRepository;
import br.com.ofisy.domain.messaging.ProcessedEvent;
import br.com.ofisy.domain.messaging.ProcessedEventRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CancelServiceOrderByQuoteService implements CancelServiceOrderByQuoteUseCase {

    private static final Set<ServiceOrderStatus> ALREADY_CLOSED = Set.of(
            ServiceOrderStatus.FINISHED,
            ServiceOrderStatus.DELIVERED,
            ServiceOrderStatus.CANCELLED);

    private final ServiceOrderRepository serviceOrderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ServiceOrderMetrics serviceOrderMetrics;

    @Override
    @Transactional
    public void execute(CancelServiceOrderByQuoteCommand cmd) {
        if (processedEventRepository.existsById(cmd.eventId())) {
            return;
        }

        ServiceOrder serviceOrder = serviceOrderRepository.findById(cmd.serviceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(cmd.serviceOrderId()));
        if (!ALREADY_CLOSED.contains(serviceOrder.getStatus())) {
            ServiceOrderStatus previousStatus = serviceOrder.getStatus();
            LocalDateTime enteredFromStatusAt = serviceOrder.getUpdatedAt();
            serviceOrder.cancel();
            ServiceOrder saved = serviceOrderRepository.save(serviceOrder);
            outboxEventRepository.save(OutboxEvent.create(
                    CancelServiceOrderService.SERVICE_ORDER_CANCELLED, cmd.serviceOrderId(), Map.of()));
            serviceOrderMetrics.recordTransition(previousStatus, saved.getStatus(), enteredFromStatusAt);
        }

        processedEventRepository.save(ProcessedEvent.create(cmd.eventId(), cmd.eventType()));
    }
}
