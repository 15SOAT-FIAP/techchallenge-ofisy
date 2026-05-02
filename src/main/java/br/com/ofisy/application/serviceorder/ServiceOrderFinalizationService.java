package br.com.ofisy.application.serviceorder;

import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceOrderFinalizationService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderExecutionRepository serviceOrderExecutionRepository;

    @Transactional
    public void cancelPending(UUID serviceOrderId) {
        List<ServiceOrderExecutionStatus> activeStatuses = List.of(
                ServiceOrderExecutionStatus.PENDING,
                ServiceOrderExecutionStatus.IN_PROGRESS
        );
        serviceOrderExecutionRepository.findByServiceOrderIdAndStatusIn(serviceOrderId, activeStatuses)
                .forEach(execution -> {
                    execution.cancel();
                    serviceOrderExecutionRepository.save(execution);
                });
    }

    @Transactional
    public void finish(UUID id) {
        var serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        serviceOrder.finish();
        serviceOrderRepository.save(serviceOrder);
    }
}
