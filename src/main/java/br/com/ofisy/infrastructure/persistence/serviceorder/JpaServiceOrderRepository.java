package br.com.ofisy.infrastructure.persistence.serviceorder;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaServiceOrderRepository extends JpaRepository<ServiceOrder, UUID> {
}
