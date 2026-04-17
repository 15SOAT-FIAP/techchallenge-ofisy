package br.com.ofisy.infrastructure.persistence.executionTime;

import br.com.ofisy.domain.executionTime.ServiceExecutionTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JpaExecutionTimeRepository extends JpaRepository<ServiceExecutionTime, UUID> {
    Optional<ServiceExecutionTime> findByServiceId(UUID serviceId);

    @Query("SELECT e FROM ServiceExecutionTime e " +
           "WHERE e.serviceId IN (SELECT s.id FROM Service s WHERE s.catalogServiceId = :catalogServiceId)")
    java.util.List<ServiceExecutionTime> findByCatalogServiceId(UUID catalogServiceId);
}

