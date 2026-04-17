package br.com.ofisy.domain.executionTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ExecutionTimeRepository {

    ServiceExecutionTime save(ServiceExecutionTime executionTime);

    Optional<ServiceExecutionTime> findById(UUID id);

    Optional<ServiceExecutionTime> findByServiceId(UUID serviceId);

    Page<ServiceExecutionTime> findAll(Pageable pageable);

    Page<ServiceExecutionTime> findByCatalogServiceId(UUID catalogServiceId, Pageable pageable);
}

