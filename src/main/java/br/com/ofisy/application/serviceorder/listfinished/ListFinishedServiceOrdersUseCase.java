package br.com.ofisy.application.serviceorder.listfinished;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListFinishedServiceOrdersUseCase {

    Page<ServiceOrder> execute(Pageable pageable);
}