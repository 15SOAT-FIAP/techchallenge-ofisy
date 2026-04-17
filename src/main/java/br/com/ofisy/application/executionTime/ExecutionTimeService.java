package br.com.ofisy.application.executionTime;

import br.com.ofisy.domain.executionTime.ExecutionTimeRepository;
import br.com.ofisy.domain.executionTime.ServiceExecutionTime;
import br.com.ofisy.infrastructure.persistence.service.JpaServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExecutionTimeService {

    private final ExecutionTimeRepository repository;
    private final JpaServiceRepository jpaServiceRepository;

    public ServiceExecutionTime create(UUID serviceId) {
        var executionTime = ServiceExecutionTime.create(serviceId);
        return repository.save(executionTime);
    }

    public ServiceExecutionTime finish(UUID executionTimeId) {
        var executionTime = repository.findById(executionTimeId)
                .orElseThrow(() -> new RuntimeException("Tempo de execução não encontrado"));
        executionTime.finish();
        return repository.save(executionTime);
    }

    public double getAverageExecutionTimeByService(UUID catalogServiceId) {
        var executionTimes = repository.findByCatalogServiceId(catalogServiceId,
                Pageable.unpaged()).getContent();

        if (executionTimes.isEmpty()) {
            return 0.0;
        }

        long totalMinutes = executionTimes.stream()
                .filter(et -> et.getEndDate() != null)
                .mapToLong(ServiceExecutionTime::getDurationInMinutes)
                .sum();

        long completedCount = executionTimes.stream()
                .filter(et -> et.getEndDate() != null)
                .count();

        return completedCount > 0 ? (double) totalMinutes / completedCount : 0.0;
    }
}

