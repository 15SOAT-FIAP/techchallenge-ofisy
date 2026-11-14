package br.com.ofisy.application.serviceorder.approve;

import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.config.metrics.ServiceOrderMetrics;
import br.com.ofisy.domain.messaging.ProcessedEvent;
import br.com.ofisy.domain.messaging.ProcessedEventRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ApproveServiceOrderService implements ApproveServiceOrderUseCase {

    private static final Set<ServiceOrderStatus> ALREADY_APPROVED = Set.of(
            ServiceOrderStatus.AWAITING_EXECUTION,
            ServiceOrderStatus.IN_PROGRESS,
            ServiceOrderStatus.FINISHED,
            ServiceOrderStatus.DELIVERED,
            ServiceOrderStatus.CANCELLED);

    private final ServiceOrderRepository serviceOrderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ServiceOrderMetrics serviceOrderMetrics;

    @Override
    @Transactional
    public void execute(ApproveServiceOrderCommand cmd) {
        if (processedEventRepository.existsById(cmd.eventId())) {
            return;
        }

        ServiceOrder serviceOrder = serviceOrderRepository.findById(cmd.serviceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(cmd.serviceOrderId()));
        if (!ALREADY_APPROVED.contains(serviceOrder.getStatus())) {
            ServiceOrderStatus previousStatus = serviceOrder.getStatus();
            LocalDateTime enteredFromStatusAt = serviceOrder.getUpdatedAt();
            serviceOrder.approve();
            ServiceOrder saved = serviceOrderRepository.save(serviceOrder);
            serviceOrderMetrics.recordTransition(previousStatus, saved.getStatus(), enteredFromStatusAt);
        }

        processedEventRepository.save(ProcessedEvent.create(cmd.eventId(), cmd.eventType()));
    }
}
