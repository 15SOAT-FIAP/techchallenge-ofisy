package br.com.ofisy.application.service;

import br.com.ofisy.application.service.dto.ServiceRequestDTO;
import br.com.ofisy.domain.service.Service;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public Service toDomain(ServiceRequestDTO dto) {
        return Service.create(
                dto.catalogServiceId(),
                dto.price()
        );
    }
}

