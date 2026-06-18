package br.com.ofisy.application.servicecatalog.identifyById;

import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.application.servicecatalog.identifybyid.IdentifyByIdServiceCatalogService;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentifyByIdServiceCatalogServiceTest {

    private static final UUID VALID_ID = UUID.randomUUID();
    private static final String VALID_NAME = "Troca de óleo";
    private static final String VALID_DESCRIPTION = "Troca de óleo e filtro";
    private static final BigDecimal VALID_PRICE = new BigDecimal("150.00");

    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;

    @InjectMocks
    private IdentifyByIdServiceCatalogService identifyByIdServiceCatalogService;

    @Nested
    class Execute {

        @Test
        void shouldReturnServiceCatalogSuccessfully() {
            var serviceCatalog = createServiceCatalog(VALID_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);
            when(serviceCatalogRepository.findById(VALID_ID)).thenReturn(Optional.of(serviceCatalog));

            ServiceCatalog result = identifyByIdServiceCatalogService.execute(VALID_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(VALID_ID);
            assertThat(result.getName()).isEqualTo(VALID_NAME);
            assertThat(result.getDescription()).isEqualTo(VALID_DESCRIPTION);
            assertThat(result.getPrice()).isEqualTo(VALID_PRICE);
            verify(serviceCatalogRepository).findById(VALID_ID);
        }

        @Test
        void shouldThrowServiceCatalogNotFoundExceptionWhenIdNotExists() {
            UUID nonExistentId = UUID.randomUUID();
            when(serviceCatalogRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> identifyByIdServiceCatalogService.execute(nonExistentId))
                    .isInstanceOf(ServiceCatalogNotFoundException.class)
                    .hasMessageContaining(nonExistentId.toString());

            verify(serviceCatalogRepository).findById(nonExistentId);
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> identifyByIdServiceCatalogService.execute(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID não pode ser nulo");
        }

        @Test
        void shouldReturnCorrectServiceCatalogWithDifferentIds() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();

            var serviceCatalog1 = createServiceCatalog(id1, "Serviço 1", "Descrição 1", new BigDecimal("100.00"));
            var serviceCatalog2 = createServiceCatalog(id2, "Serviço 2", "Descrição 2", new BigDecimal("200.00"));

            when(serviceCatalogRepository.findById(id1)).thenReturn(Optional.of(serviceCatalog1));
            when(serviceCatalogRepository.findById(id2)).thenReturn(Optional.of(serviceCatalog2));

            ServiceCatalog result1 = identifyByIdServiceCatalogService.execute(id1);
            ServiceCatalog result2 = identifyByIdServiceCatalogService.execute(id2);

            assertThat(result1.getId()).isEqualTo(id1);
            assertThat(result1.getName()).isEqualTo("Serviço 1");
            assertThat(result2.getId()).isEqualTo(id2);
            assertThat(result2.getName()).isEqualTo("Serviço 2");
        }

        @Test
        void shouldThrowNotFoundExceptionWithCorrectMessage() {
            UUID id = UUID.randomUUID();
            when(serviceCatalogRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> identifyByIdServiceCatalogService.execute(id))
                    .isInstanceOf(ServiceCatalogNotFoundException.class)
                    .hasMessage("Serviço não encontrado com ID: " + id);
        }
    }

    private ServiceCatalog createServiceCatalog(UUID id, String name, String description, BigDecimal price) {
        return ServiceCatalog.reconstruct(id, name, description, price, LocalDateTime.now(), LocalDateTime.now());
    }
}

