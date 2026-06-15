package br.com.ofisy.application.serviceorder.delivertocustomer;

import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliverToCustomerService implements DeliverToCustomerUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    @Override
    @Transactional
    public ServiceOrder execute(UUID id) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        serviceOrder.deliver();
        return serviceOrderRepository.save(serviceOrder);
    }
}