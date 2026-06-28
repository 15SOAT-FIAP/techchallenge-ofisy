package br.com.ofisy.adapters.gateways.serviceorderexecution;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderExecutionRepositoryImplTest {

    @Mock
    private JpaServiceOrderExecutionRepository jpa;

    private ServiceOrderExecutionRepositoryImpl repository;


    @BeforeEach
    void setUp() {
        repository = new ServiceOrderExecutionRepositoryImpl(jpa);
    }


    @Test
    void shouldSaveServiceOrderExecution() {
        ServiceOrderExecution domain = mock(ServiceOrderExecution.class);
        ServiceOrderExecutionEntity entity = ServiceOrderExecutionMapper.toEntity(domain);

        when(jpa.save(any(ServiceOrderExecutionEntity.class)))
                .thenReturn(entity);

        ServiceOrderExecution result = repository.save(domain);

        assertNotNull(result);

        verify(jpa)
                .save(any(ServiceOrderExecutionEntity.class));
    }


    @Test
    void shouldFindAllServiceOrderExecutions() {
        Pageable pageable = PageRequest.of(0, 10);

        ServiceOrderExecutionEntity entity =
                mock(ServiceOrderExecutionEntity.class);

        Page<ServiceOrderExecutionEntity> page =
                new PageImpl<>(List.of(entity));

        when(jpa.findAll(pageable))
                .thenReturn(page);

        Page<ServiceOrderExecution> result =
                repository.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(jpa)
                .findAll(pageable);
    }


    @Test
    void shouldFindById() {
        UUID id = UUID.randomUUID();

        ServiceOrderExecutionEntity entity =
                mock(ServiceOrderExecutionEntity.class);

        when(jpa.findById(id))
                .thenReturn(Optional.of(entity));

        Optional<ServiceOrderExecution> result =
                repository.findById(id);

        assertTrue(result.isPresent());

        verify(jpa)
                .findById(id);
    }


    @Test
    void shouldReturnEmptyWhenServiceOrderExecutionNotFound() {
        UUID id = UUID.randomUUID();

        when(jpa.findById(id))
                .thenReturn(Optional.empty());

        Optional<ServiceOrderExecution> result =
                repository.findById(id);

        assertTrue(result.isEmpty());

        verify(jpa)
                .findById(id);
    }


    @Test
    void shouldFindByServiceCatalogId() {
        UUID serviceCatalogId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 5);

        when(jpa.findByServiceCatalogId(serviceCatalogId, pageable))
                .thenReturn(Page.empty());

        Page<ServiceOrderExecution> result =
                repository.findByServiceCatalogId(serviceCatalogId, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(jpa)
                .findByServiceCatalogId(serviceCatalogId, pageable);
    }


    @Test
    void shouldFindByServiceOrderId() {
        UUID serviceOrderId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 5);

        when(jpa.findByServiceOrderId(serviceOrderId, pageable))
                .thenReturn(Page.empty());

        Page<ServiceOrderExecution> result =
                repository.findByServiceOrderId(serviceOrderId, pageable);


        assertNotNull(result);

        verify(jpa)
                .findByServiceOrderId(serviceOrderId, pageable);
    }


    @Test
    void shouldFindByStatus() {
        Pageable pageable = PageRequest.of(0, 10);

        when(jpa.findByStatus(
                ServiceOrderExecutionStatus.COMPLETED,
                pageable))
                .thenReturn(Page.empty());


        Page<ServiceOrderExecution> result =
                repository.findByStatus(
                        ServiceOrderExecutionStatus.COMPLETED,
                        pageable);


        assertNotNull(result);

        verify(jpa)
                .findByStatus(
                        ServiceOrderExecutionStatus.COMPLETED,
                        pageable);
    }


    @Test
    void shouldCountByServiceOrderId() {
        UUID serviceOrderId = UUID.randomUUID();

        when(jpa.countByServiceOrderId(serviceOrderId))
                .thenReturn(10L);


        long result =
                repository.countByServiceOrderId(serviceOrderId);


        assertEquals(10L, result);

        verify(jpa)
                .countByServiceOrderId(serviceOrderId);
    }


    @Test
    void shouldCountByServiceOrderIdAndStatus() {
        UUID serviceOrderId = UUID.randomUUID();

        when(jpa.countByServiceOrderIdAndStatus(
                serviceOrderId,
                ServiceOrderExecutionStatus.PENDING))
                .thenReturn(3L);


        long result =
                repository.countByServiceOrderIdAndStatus(
                        serviceOrderId,
                        ServiceOrderExecutionStatus.PENDING);


        assertEquals(3L, result);

        verify(jpa)
                .countByServiceOrderIdAndStatus(
                        serviceOrderId,
                        ServiceOrderExecutionStatus.PENDING);
    }


    @Test
    void shouldFindByServiceOrderIdAndStatusIn() {
        UUID serviceOrderId = UUID.randomUUID();

        Collection<ServiceOrderExecutionStatus> statuses =
                List.of(
                        ServiceOrderExecutionStatus.PENDING,
                        ServiceOrderExecutionStatus.COMPLETED
                );

        when(jpa.findByServiceOrderIdAndStatusIn(
                serviceOrderId,
                statuses))
                .thenReturn(List.of());


        List<ServiceOrderExecution> result =
                repository.findByServiceOrderIdAndStatusIn(
                        serviceOrderId,
                        statuses);


        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(jpa)
                .findByServiceOrderIdAndStatusIn(
                        serviceOrderId,
                        statuses);
    }
}