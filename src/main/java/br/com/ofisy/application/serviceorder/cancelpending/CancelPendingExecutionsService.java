package br.com.ofisy.application.serviceorder.cancelpending;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelPendingExecutionsService implements CancelPendingExecutionsUseCase {

    private final ServiceOrderExecutionRepository serviceOrderExecutionRepository;

    @Override
    @Transactional
    public void execute(UUID serviceOrderId) {
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
}