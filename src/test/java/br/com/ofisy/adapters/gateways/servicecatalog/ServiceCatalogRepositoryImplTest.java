package br.com.ofisy.adapters.gateways.servicecatalog;

import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
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
        ServiceCatalogEntity entity = ServiceCatalogEntity.builder()
                .id(serviceCatalog.getId())
                .name(serviceCatalog.getName())
                .description(serviceCatalog.getDescription())
                .price(serviceCatalog.getPrice())
                .createdAt(serviceCatalog.getCreatedAt())
                .updatedAt(serviceCatalog.getUpdatedAt())
                .build();

        when(jpa.save(any(ServiceCatalogEntity.class))).thenReturn(entity);

        ServiceCatalog result = repository.save(serviceCatalog);

        assertNotNull(result);
        assertEquals(serviceCatalog.getName(), result.getName());
        verify(jpa, times(1)).save(any(ServiceCatalogEntity.class));
    }

    @Test
    void findAll_shouldDelegateToJpa() {
        Pageable pageable = PageRequest.of(0, 10);
        ServiceCatalogEntity entity = ServiceCatalogEntity.builder()
                .id(serviceCatalog.getId())
                .name(serviceCatalog.getName())
                .description(serviceCatalog.getDescription())
                .price(serviceCatalog.getPrice())
                .createdAt(serviceCatalog.getCreatedAt())
                .updatedAt(serviceCatalog.getUpdatedAt())
                .build();

        Page<ServiceCatalogEntity> page = new PageImpl<>(List.of(entity), pageable, 1);
        when(jpa.findAll(pageable)).thenReturn(page);

        Page<ServiceCatalog> result = repository.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(serviceCatalog.getName(), result.getContent().get(0).getName());
        verify(jpa, times(1)).findAll(pageable);
    }

    @Test
    void findById_shouldDelegateToJpa() {
        ServiceCatalogEntity entity = ServiceCatalogEntity.builder()
                .id(serviceCatalogId)
                .name(serviceCatalog.getName())
                .description(serviceCatalog.getDescription())
                .price(serviceCatalog.getPrice())
                .createdAt(serviceCatalog.getCreatedAt())
                .updatedAt(serviceCatalog.getUpdatedAt())
                .build();

        when(jpa.findById(serviceCatalogId)).thenReturn(Optional.of(entity));

        Optional<ServiceCatalog> result = repository.findById(serviceCatalogId);

        assertTrue(result.isPresent());
        assertEquals(serviceCatalog.getName(), result.get().getName());
        verify(jpa, times(1)).findById(serviceCatalogId);
    }

    @Test
    void findByName_shouldDelegateToJpa() {
        String name = "Oil Change";
        ServiceCatalogEntity entity = ServiceCatalogEntity.builder()
                .id(serviceCatalog.getId())
                .name(serviceCatalog.getName())
                .description(serviceCatalog.getDescription())
                .price(serviceCatalog.getPrice())
                .createdAt(serviceCatalog.getCreatedAt())
                .updatedAt(serviceCatalog.getUpdatedAt())
                .build();

        when(jpa.findByName(name)).thenReturn(Optional.of(entity));

        Optional<ServiceCatalog> result = repository.findByName(name);

        assertTrue(result.isPresent());
        assertEquals(serviceCatalog.getName(), result.get().getName());
        verify(jpa, times(1)).findByName(name);
    }


        @Test
        void shouldConvertEntityToDomain() {
            ServiceCatalogEntity entity = ServiceCatalogEntity.builder()
                    .id(UUID.randomUUID())
                    .name("Alinhamento")
                    .description("Serviço de alinhamento de rodas")
                    .price(new BigDecimal("200.00"))
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();

            ServiceCatalog domain = ServiceCatalogMapper.toDomain(entity);

            assertNotNull(domain);
            assertEquals("Alinhamento", domain.getName());
            assertEquals("Serviço de alinhamento de rodas", domain.getDescription());
            assertEquals(new BigDecimal("200.00"), domain.getPrice());
        }

        @Test
        void shouldConvertDomainToEntity() {
            ServiceCatalog serviceCatalog = ServiceCatalog.create(
                    "Alinhamento",
                    "Serviço de alinhamento de rodas",
                    new BigDecimal("200.00")
            );

            ServiceCatalogEntity entity = ServiceCatalogMapper.toEntity(serviceCatalog);

            assertNotNull(entity);
            assertEquals(serviceCatalog.getId(), entity.getId());
            assertEquals("Alinhamento", entity.getName());
            assertEquals("Serviço de alinhamento de rodas", entity.getDescription());
            assertEquals(new BigDecimal("200.00"), entity.getPrice());
            assertEquals(serviceCatalog.getCreatedAt(), entity.getCreatedAt());
            assertEquals(serviceCatalog.getUpdatedAt(), entity.getUpdatedAt());
        }
}
