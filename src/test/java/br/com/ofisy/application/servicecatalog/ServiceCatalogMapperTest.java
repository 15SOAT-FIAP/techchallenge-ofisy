package br.com.ofisy.application.servicecatalog;

import br.com.ofisy.application.servicecatalog.dto.ServiceCatalogRequestDTO;
import br.com.ofisy.application.servicecatalog.dto.ServiceCatalogResponseDTO;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServiceCatalogMapperTest {

    @Test
    void shouldConvertRequestDTOToDomain() {
        ServiceCatalogRequestDTO request = new ServiceCatalogRequestDTO(
                new BigDecimal("150.00"),
                "Troca de Óleo",
                "Serviço de troca de óleo do motor"
        );

        ServiceCatalog serviceCatalog = ServiceCatalogMapper.toDomain(request);

        assertNotNull(serviceCatalog);
        assertEquals("Troca de Óleo", serviceCatalog.getName());
        assertEquals("Serviço de troca de óleo do motor", serviceCatalog.getDescription());
        assertEquals(new BigDecimal("150.00"), serviceCatalog.getPrice());
    }

    @Test
    void shouldThrowExceptionWhenRequestDTOIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceCatalogMapper.toDomain(null)
        );

        assertEquals(
                "ServiceCatalogResponseDTO não pode ser nulo",
                exception.getMessage()
        );
    }

    @Test
    void shouldConvertDomainToResponseDTO() {
        ServiceCatalog serviceCatalog = ServiceCatalog.create(
                "Alinhamento",
                "Serviço de alinhamento de rodas",
                new BigDecimal("200.00")
        );

        ServiceCatalogResponseDTO response = ServiceCatalogMapper.toDTO(serviceCatalog);

        assertNotNull(response);
        assertEquals(serviceCatalog.getId(), response.id());
        assertEquals("Alinhamento", response.name());
        assertEquals("Serviço de alinhamento de rodas", response.description());
        assertEquals(new BigDecimal("200.00"), response.price());
        assertEquals(serviceCatalog.getCreatedAt(), response.createdAt());
        assertEquals(serviceCatalog.getUpdatedAt(), response.updatedAt());
    }

    @Test
    void shouldThrowExceptionWhenDomainIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceCatalogMapper.toDTO(null)
        );

        assertEquals(
                "ServiceCatalog não pode ser nulo",
                exception.getMessage()
        );
    }
}