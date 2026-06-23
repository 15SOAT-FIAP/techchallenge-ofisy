package br.com.ofisy.application.serviceorder.listactive;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListActiveServiceOrdersService implements ListActiveServiceOrdersUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceOrder> execute(Pageable pageable) {
        return serviceOrderRepository.findActive(pageable);
    }
}
