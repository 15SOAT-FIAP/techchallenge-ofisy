package br.com.ofisy.application.serviceorder.listactive;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListActiveServiceOrdersUseCase {

    Page<ServiceOrder> execute(Pageable pageable);
}
