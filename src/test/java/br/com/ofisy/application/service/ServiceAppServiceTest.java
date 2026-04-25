package br.com.ofisy.application.service;

import br.com.ofisy.application.service.dto.ServiceRequestDTO;
import br.com.ofisy.application.service.exceptions.ServiceNotFoundException;
import br.com.ofisy.domain.service.Service;
import br.com.ofisy.domain.service.ServiceRepository;
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
class ServiceAppServiceTest {

    @Mock
    private ServiceRepository repository;

    @InjectMocks
    private ServiceAppService serviceAppService;

    private ServiceRequestDTO requestDTO;
    private Service service;
    private UUID serviceId;

    @BeforeEach
    void setUp() {
        serviceId = UUID.randomUUID();
        requestDTO = new ServiceRequestDTO(
                new BigDecimal("50.00"),
                "Oil Change",
                "Change engine oil"
        );
        service = Service.create("Oil Change", "Change engine oil", new BigDecimal("50.00"));
    }

    @Test
    void create_shouldSaveAndReturnService() {
        when(repository.save(any(Service.class))).thenReturn(service);

        Service result = serviceAppService.create(requestDTO);

        assertNotNull(result);
        assertEquals(requestDTO.name(), result.getName());
        assertEquals(requestDTO.description(), result.getDescription());
        assertEquals(requestDTO.price(), result.getPrice());
        verify(repository, times(1)).save(any(Service.class));
    }

    @Test
    void findById_shouldReturnServiceWhenFound() {
        when(repository.findById(serviceId)).thenReturn(Optional.of(service));

        Service result = serviceAppService.findById(serviceId);

        assertNotNull(result);
        assertEquals(service, result);
        verify(repository, times(1)).findById(serviceId);
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {
        when(repository.findById(serviceId)).thenReturn(Optional.empty());

        ServiceNotFoundException exception = assertThrows(ServiceNotFoundException.class,
                () -> serviceAppService.findById(serviceId));
        assertEquals("Serviço não encontrado com ID: " + serviceId, exception.getMessage());
        verify(repository, times(1)).findById(serviceId);
    }

    @Test
    void findAll_shouldReturnPageOfServices() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Service> page = new PageImpl<>(List.of(service), pageable, 1);
        when(repository.findAll(pageable)).thenReturn(page);

        Page<Service> result = serviceAppService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(service, result.getContent().get(0));
        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    void findByName_shouldReturnServiceWhenFound() {
        String name = "Oil Change";
        when(repository.findByName(name)).thenReturn(Optional.of(service));

        Service result = serviceAppService.findByName(name);

        assertNotNull(result);
        assertEquals(service, result);
        verify(repository, times(1)).findByName(name);
    }

    @Test
    void findByName_shouldThrowExceptionWhenNotFound() {
        String name = "Nonexistent Service";
        when(repository.findByName(name)).thenReturn(Optional.empty());

        ServiceNotFoundException exception = assertThrows(ServiceNotFoundException.class,
                () -> serviceAppService.findByName(name));
        assertEquals("Serviço não encontrado com ID: " + name, exception.getMessage());
        verify(repository, times(1)).findByName(name);
    }
}
