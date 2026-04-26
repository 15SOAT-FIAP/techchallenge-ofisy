package br.com.ofisy.infrastructure.persistence.service;

import br.com.ofisy.domain.service.Service;
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
class ServiceRepositoryImplTest {

    @Mock
    private JpaServiceRepository jpa;

    @InjectMocks
    private ServiceRepositoryImpl repository;

    private Service service;
    private UUID serviceId;

    @BeforeEach
    void setUp() {
        serviceId = UUID.randomUUID();
        service = Service.create("Oil Change", "Change engine oil", new BigDecimal("50.00"));
    }

    @Test
    void save_shouldDelegateToJpa() {
        when(jpa.save(any(Service.class))).thenReturn(service);

        Service result = repository.save(service);

        assertNotNull(result);
        assertEquals(service, result);
        verify(jpa, times(1)).save(service);
    }

    @Test
    void findAll_shouldDelegateToJpa() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Service> page = new PageImpl<>(List.of(service), pageable, 1);
        when(jpa.findAll(pageable)).thenReturn(page);

        Page<Service> result = repository.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(service, result.getContent().get(0));
        verify(jpa, times(1)).findAll(pageable);
    }

    @Test
    void findById_shouldDelegateToJpa() {
        when(jpa.findById(serviceId)).thenReturn(Optional.of(service));

        Optional<Service> result = repository.findById(serviceId);

        assertTrue(result.isPresent());
        assertEquals(service, result.get());
        verify(jpa, times(1)).findById(serviceId);
    }

    @Test
    void findByName_shouldDelegateToJpa() {
        String name = "Oil Change";
        when(jpa.findByName(name)).thenReturn(Optional.of(service));

        Optional<Service> result = repository.findByName(name);

        assertTrue(result.isPresent());
        assertEquals(service, result.get());
        verify(jpa, times(1)).findByName(name);
    }
}
