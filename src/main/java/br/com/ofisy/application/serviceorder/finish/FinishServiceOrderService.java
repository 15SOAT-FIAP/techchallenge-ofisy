package br.com.ofisy.application.serviceorder.finish;

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
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class FinishServiceOrderService implements FinishServiceOrderUseCase {

    public static final String SERVICE_ORDER_FINISHED = "ServiceOrderFinished";

    private static final Set<ServiceOrderStatus> ALREADY_FINISHED = Set.of(
            ServiceOrderStatus.FINISHED,
            ServiceOrderStatus.DELIVERED,
            ServiceOrderStatus.CANCELLED);

    private final ServiceOrderRepository serviceOrderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ServiceOrderMetrics serviceOrderMetrics;

    @Override
    @Transactional
    public void execute(FinishServiceOrderCommand cmd) {
        if (processedEventRepository.existsById(cmd.eventId())) {
            return;
        }

        ServiceOrder serviceOrder = serviceOrderRepository.findById(cmd.serviceOrderId())
                .orElseThrow(() -> new ServiceOrderNotFoundException(cmd.serviceOrderId()));
        if (!ALREADY_FINISHED.contains(serviceOrder.getStatus())) {
            if (serviceOrder.getStatus() == ServiceOrderStatus.AWAITING_EXECUTION) {
                serviceOrder = transition(serviceOrder, ServiceOrder::startExecution);
            }
            transition(serviceOrder, ServiceOrder::finish);
            outboxEventRepository.save(OutboxEvent.create(SERVICE_ORDER_FINISHED, cmd.serviceOrderId(), Map.of()));
        }

        processedEventRepository.save(ProcessedEvent.create(cmd.eventId(), cmd.eventType()));
    }

    private ServiceOrder transition(ServiceOrder serviceOrder, Consumer<ServiceOrder> change) {
        ServiceOrderStatus previousStatus = serviceOrder.getStatus();
        LocalDateTime enteredFromStatusAt = serviceOrder.getUpdatedAt();
        change.accept(serviceOrder);
        ServiceOrder saved = serviceOrderRepository.save(serviceOrder);
        serviceOrderMetrics.recordTransition(previousStatus, saved.getStatus(), enteredFromStatusAt);
        return saved;
    }
}