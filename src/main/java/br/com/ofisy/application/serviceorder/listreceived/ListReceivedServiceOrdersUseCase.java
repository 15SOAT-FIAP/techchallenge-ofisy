package br.com.ofisy.application.serviceorder.listreceived;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListReceivedServiceOrdersUseCase {

    Page<ServiceOrder> execute(Pageable pageable);
}