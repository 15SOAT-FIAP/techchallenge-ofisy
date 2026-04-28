package br.com.ofisy.infrastructure.persistence.serviceorder;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaServiceOrderRepository extends JpaRepository<ServiceOrder, UUID> {
    Page<ServiceOrder> findByStatus(ServiceOrderStatus serviceOrderStatus, Pageable pageable);
}
