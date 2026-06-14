package br.com.ofisy.application.serviceorder.startexecution;

import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StartServiceOrderExecutionService implements StartServiceOrderExecutionUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    @Override
    @Transactional
    public void execute(UUID id) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        if (serviceOrder.getStatus() == ServiceOrderStatus.AWAITING_EXECUTION) {
            serviceOrder.startExecution();
            serviceOrderRepository.save(serviceOrder);
        }
    }
}