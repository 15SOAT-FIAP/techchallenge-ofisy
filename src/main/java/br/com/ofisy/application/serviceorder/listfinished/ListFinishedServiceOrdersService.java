package br.com.ofisy.application.serviceorder.listfinished;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListFinishedServiceOrdersService implements ListFinishedServiceOrdersUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceOrder> execute(Pageable pageable) {
        return serviceOrderRepository.findByStatus(ServiceOrderStatus.FINISHED, pageable);
    }
}