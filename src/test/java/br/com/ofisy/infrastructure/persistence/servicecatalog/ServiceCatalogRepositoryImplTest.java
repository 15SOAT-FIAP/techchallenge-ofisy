package br.com.ofisy.infrastructure.persistence.servicecatalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceCatalogRepositoryImplTest {

    @Mock
    private JpaServiceCatalogRepository jpa;

    @InjectMocks
    private ServiceCatalogRepositoryImpl repository;

    private ServiceCatalog serviceCatalog;
    private UUID serviceCatalogId;

    @BeforeEach
    void setUp() {
        serviceCatalogId = UUID.randomUUID();
        serviceCatalog = br.com.ofisy.domain.servicecatalog.ServiceCatalog.create("Oil Change", "Change engine oil", new BigDecimal("50.00"));
    }

    @Test
    void save_shouldDelegateToJpa() {
        when(jpa.save(any(ServiceCatalog.class))).thenReturn(serviceCatalog);

        ServiceCatalog result = repository.save(serviceCatalog);

        assertNotNull(result);
        assertEquals(serviceCatalog, result);
        verify(jpa, times(1)).save(serviceCatalog);
    }

    @Test
    void findAll_shouldDelegateToJpa() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceCatalog> page = new PageImpl<>(List.of(serviceCatalog), pageable, 1);
        when(jpa.findAll(pageable)).thenReturn(page);

        Page<ServiceCatalog> result = repository.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(serviceCatalog, result.getContent().get(0));
        verify(jpa, times(1)).findAll(pageable);
    }

    @Test
    void findById_shouldDelegateToJpa() {
        when(jpa.findById(serviceCatalogId)).thenReturn(Optional.of(serviceCatalog));

        Optional<ServiceCatalog> result = repository.findById(serviceCatalogId);

        assertTrue(result.isPresent());
        assertEquals(serviceCatalog, result.get());
        verify(jpa, times(1)).findById(serviceCatalogId);
    }

    @Test
    void findByName_shouldDelegateToJpa() {
        String name = "Oil Change";
        when(jpa.findByName(name)).thenReturn(Optional.of(serviceCatalog));

        Optional<ServiceCatalog> result = repository.findByName(name);

        assertTrue(result.isPresent());
        assertEquals(serviceCatalog, result.get());
        verify(jpa, times(1)).findByName(name);
    }
}
