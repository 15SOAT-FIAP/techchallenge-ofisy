package br.com.ofisy.adapters.gateways.serviceorder;

import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface JpaServiceOrderRepository extends JpaRepository<ServiceOrderEntity, UUID> {

    Page<ServiceOrderEntity> findByStatus(ServiceOrderStatus serviceOrderStatus, Pageable pageable);

    @Query("SELECT s FROM ServiceOrderEntity s WHERE s.priority > 0 ORDER BY s.priority ASC, s.createdAt ASC")
    Page<ServiceOrderEntity> findActiveOrderedByPriority(Pageable pageable);
}