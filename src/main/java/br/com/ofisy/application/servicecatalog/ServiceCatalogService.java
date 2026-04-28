package br.com.ofisy.application.servicecatalog;

import br.com.ofisy.application.servicecatalog.dto.ServiceCatalogRequestDTO;
import br.com.ofisy.application.servicecatalog.dto.ServiceCatalogResponseDTO;
import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final ServiceCatalogRepository repository;

    public ServiceCatalogResponseDTO create(ServiceCatalogRequestDTO dto) {
        ServiceCatalog serviceCatalog = ServiceCatalogMapper.toDomain(dto);

        ServiceCatalog savedServiceCatalog = repository.save(serviceCatalog);

        return ServiceCatalogMapper.toDTO(savedServiceCatalog);
    }

    public ServiceCatalogResponseDTO findById(UUID id) {
        return repository.findById(id)
                .map(ServiceCatalogMapper::toDTO)
                .orElseThrow(() -> new ServiceCatalogNotFoundException(id.toString()));
    }

    public Page<ServiceCatalogResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(ServiceCatalogMapper::toDTO);
    }

    public ServiceCatalogResponseDTO findByName(String name) {
        return repository.findByName(name)
                .map(ServiceCatalogMapper::toDTO)
                .orElseThrow(() -> new ServiceCatalogNotFoundException(name));
    }
}
