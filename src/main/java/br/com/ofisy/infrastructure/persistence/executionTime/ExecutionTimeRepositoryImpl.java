package br.com.ofisy.infrastructure.persistence.executionTime;

import br.com.ofisy.domain.executionTime.ExecutionTimeRepository;
import br.com.ofisy.domain.executionTime.ServiceExecutionTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ExecutionTimeRepositoryImpl implements ExecutionTimeRepository {

    private final JpaExecutionTimeRepository jpa;

    @Override
    public ServiceExecutionTime save(ServiceExecutionTime executionTime) {
        return jpa.save(executionTime);
    }

    @Override
    public Optional<ServiceExecutionTime> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<ServiceExecutionTime> findByServiceId(UUID serviceId) {
        return jpa.findByServiceId(serviceId);
    }

    @Override
    public Page<ServiceExecutionTime> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }

    @Override
    public Page<ServiceExecutionTime> findByCatalogServiceId(UUID catalogServiceId, Pageable pageable) {
        var times = jpa.findByCatalogServiceId(catalogServiceId);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), times.size());
        return new PageImpl<>(
                times.subList(start, end),
                pageable,
                times.size()
        );
    }
}

