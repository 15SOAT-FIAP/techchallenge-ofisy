package br.com.ofisy.application.serviceorder.cancel;

import br.com.ofisy.application.serviceorder.cancelpending.CancelPendingExecutionsUseCase;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelServiceOrderService implements CancelServiceOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final CancelPendingExecutionsUseCase cancelPendingExecutionsUseCase;

    @Override
    @Transactional
    public ServiceOrder execute(UUID id) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        cancelPendingExecutionsUseCase.execute(id);
        serviceOrder.cancel();
        return serviceOrderRepository.save(serviceOrder);
    }
}