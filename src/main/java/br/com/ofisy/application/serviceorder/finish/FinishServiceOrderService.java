package br.com.ofisy.application.serviceorder.finish;

import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.config.metrics.ServiceOrderMetrics;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinishServiceOrderService implements FinishServiceOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderMetrics serviceOrderMetrics;

    @Override
    @Transactional
    public void execute(UUID id) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        ServiceOrderStatus previousStatus = serviceOrder.getStatus();
        LocalDateTime enteredFromStatusAt = serviceOrder.getUpdatedAt();
        serviceOrder.finish();
        ServiceOrder saved = serviceOrderRepository.save(serviceOrder);
        serviceOrderMetrics.recordTransition(previousStatus, saved.getStatus(), enteredFromStatusAt);
    }
}