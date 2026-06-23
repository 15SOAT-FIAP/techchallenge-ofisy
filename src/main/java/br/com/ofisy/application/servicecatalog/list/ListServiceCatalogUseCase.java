package br.com.ofisy.application.servicecatalog.list;

import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListServiceCatalogUseCase {
    Page<ServiceCatalog> execute(Pageable pageable);

}
