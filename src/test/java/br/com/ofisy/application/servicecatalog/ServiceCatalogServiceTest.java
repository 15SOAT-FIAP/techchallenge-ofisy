package br.com.ofisy.application.servicecatalog;

import br.com.ofisy.application.servicecatalog.dto.ServiceCatalogRequestDTO;
import br.com.ofisy.application.servicecatalog.dto.ServiceCatalogResponseDTO;
import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
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
class ServiceCatalogServiceTest {

    @Mock
    private ServiceCatalogRepository repository;

    @InjectMocks
    private ServiceCatalogService serviceCatalogService;

    private ServiceCatalogRequestDTO requestDTO;
    private ServiceCatalog serviceCatalog;
    private UUID serviceCatalogId;

    @BeforeEach
    void setUp() {
        serviceCatalogId = UUID.randomUUID();
        requestDTO = new ServiceCatalogRequestDTO(
                new BigDecimal("50.00"),
                "Oil Change",
                "Change engine oil"
        );
        serviceCatalog = ServiceCatalog.create("Oil Change", "Change engine oil", new BigDecimal("50.00"));
        // Set the ID using reflection since it's private
        try {
            java.lang.reflect.Field idField = ServiceCatalog.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(serviceCatalog, serviceCatalogId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void create_shouldSaveAndReturnService() {
        when(repository.save(any(ServiceCatalog.class))).thenReturn(serviceCatalog);

        ServiceCatalogResponseDTO result = serviceCatalogService.create(requestDTO);

        assertNotNull(result);
        assertEquals(requestDTO.name(), result.name());
        assertEquals(requestDTO.description(), result.description());
        assertEquals(requestDTO.price(), result.price());
        verify(repository, times(1)).save(any(ServiceCatalog.class));
    }

    @Test
    void findById_shouldReturnServiceWhenFound() {
        when(repository.findById(serviceCatalogId)).thenReturn(Optional.of(serviceCatalog));

        ServiceCatalogResponseDTO result = serviceCatalogService.findById(serviceCatalogId);

        assertNotNull(result);
        assertEquals(serviceCatalog.getId(), result.id());
        assertEquals(serviceCatalog.getName(), result.name());
        assertEquals(serviceCatalog.getDescription(), result.description());
        assertEquals(serviceCatalog.getPrice(), result.price());
        verify(repository, times(1)).findById(serviceCatalogId);
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {
        when(repository.findById(serviceCatalogId)).thenReturn(Optional.empty());

        ServiceCatalogNotFoundException exception = assertThrows(ServiceCatalogNotFoundException.class,
                () -> serviceCatalogService.findById(serviceCatalogId));
        assertEquals("Serviço não encontrado com ID: " + serviceCatalogId, exception.getMessage());
        verify(repository, times(1)).findById(serviceCatalogId);
    }

    @Test
    void findAll_shouldReturnPageOfServices() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceCatalog> page = new PageImpl<>(List.of(serviceCatalog), pageable, 1);
        when(repository.findAll(pageable)).thenReturn(page);

        Page<ServiceCatalogResponseDTO> result = serviceCatalogService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(serviceCatalog.getId(), result.getContent().get(0).id());
        assertEquals(serviceCatalog.getName(), result.getContent().get(0).name());
        assertEquals(serviceCatalog.getDescription(), result.getContent().get(0).description());
        assertEquals(serviceCatalog.getPrice(), result.getContent().get(0).price());
        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    void findByName_shouldReturnServiceWhenFound() {
        String name = "Oil Change";
        when(repository.findByName(name)).thenReturn(Optional.of(serviceCatalog));

        ServiceCatalogResponseDTO result = serviceCatalogService.findByName(name);

        assertNotNull(result);
        assertEquals(serviceCatalog.getId(), result.id());
        assertEquals(serviceCatalog.getName(), result.name());
        assertEquals(serviceCatalog.getDescription(), result.description());
        assertEquals(serviceCatalog.getPrice(), result.price());
        verify(repository, times(1)).findByName(name);
    }

    @Test
    void findByName_shouldThrowExceptionWhenNotFound() {
        String name = "Nonexistent ServiceCatalog";
        when(repository.findByName(name)).thenReturn(Optional.empty());

        ServiceCatalogNotFoundException exception = assertThrows(ServiceCatalogNotFoundException.class,
                () -> serviceCatalogService.findByName(name));
        assertEquals("Serviço não encontrado com nome: " + name, exception.getMessage());
        verify(repository, times(1)).findByName(name);
    }
}
