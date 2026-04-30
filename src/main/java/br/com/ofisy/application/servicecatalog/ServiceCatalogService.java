package br.com.ofisy.application.servicecatalog;

import br.com.ofisy.application.servicecatalog.dto.ServiceCatalogRequestDTO;
import br.com.ofisy.application.servicecatalog.dto.ServiceCatalogResponseDTO;
import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final ServiceCatalogRepository repository;

    @Transactional
    public ServiceCatalogResponseDTO create(ServiceCatalogRequestDTO dto) {
        ServiceCatalog serviceCatalog = ServiceCatalogMapper.toDomain(dto);

        ServiceCatalog savedServiceCatalog = repository.save(serviceCatalog);

        return ServiceCatalogMapper.toDTO(savedServiceCatalog);
    }

    @Transactional(readOnly = true)
    public ServiceCatalogResponseDTO findById(UUID id) {
        return repository.findById(id)
                .map(ServiceCatalogMapper::toDTO)
                .orElseThrow(() -> ServiceCatalogNotFoundException.ofId(id.toString()));
    }

    @Transactional(readOnly = true)
    public Page<ServiceCatalogResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(ServiceCatalogMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ServiceCatalogResponseDTO findByName(String name) {
        return repository.findByName(name)
                .map(ServiceCatalogMapper::toDTO)
                .orElseThrow(() -> ServiceCatalogNotFoundException.ofName(name));
    }
}
