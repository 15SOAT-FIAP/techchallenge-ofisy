package br.com.ofisy.application.servicecatalog.identifybyname;

import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
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
class IdentifyByNameServiceCatalogServiceTest {

    private static final String VALID_NAME = "Troca de óleo";
    private static final String VALID_DESCRIPTION = "Troca de óleo e filtro";
    private static final BigDecimal VALID_PRICE = new BigDecimal("150.00");

    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;

    @InjectMocks
    private IdentifyByNameServiceCatalogService identifyByNameServiceCatalogService;

    @Nested
    class Execute {

        @Test
        void shouldReturnServiceCatalogSuccessfully() {
            var serviceCatalog = createServiceCatalog(VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);
            when(serviceCatalogRepository.findByName(VALID_NAME)).thenReturn(Optional.of(serviceCatalog));

            ServiceCatalog result = identifyByNameServiceCatalogService.execute(VALID_NAME);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(VALID_NAME);
            assertThat(result.getDescription()).isEqualTo(VALID_DESCRIPTION);
            assertThat(result.getPrice()).isEqualTo(VALID_PRICE);
            verify(serviceCatalogRepository).findByName(VALID_NAME);
        }

        @Test
        void shouldThrowServiceCatalogNotFoundExceptionWhenNameNotExists() {
            String nonExistentName = "Serviço que não existe";
            when(serviceCatalogRepository.findByName(nonExistentName)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> identifyByNameServiceCatalogService.execute(nonExistentName))
                    .isInstanceOf(ServiceCatalogNotFoundException.class)
                    .hasMessageContaining(nonExistentName);

            verify(serviceCatalogRepository).findByName(nonExistentName);
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenNameIsNull() {
            assertThatThrownBy(() -> identifyByNameServiceCatalogService.execute(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome não pode ser nulo");
        }

        @Test
        void shouldReturnCorrectServiceCatalogWithDifferentNames() {
            String name1 = "Troca de óleo";
            String name2 = "Revisão completa";

            var serviceCatalog1 = createServiceCatalog(name1, "Descrição 1", new BigDecimal("100.00"));
            var serviceCatalog2 = createServiceCatalog(name2, "Descrição 2", new BigDecimal("200.00"));

            when(serviceCatalogRepository.findByName(name1)).thenReturn(Optional.of(serviceCatalog1));
            when(serviceCatalogRepository.findByName(name2)).thenReturn(Optional.of(serviceCatalog2));

            ServiceCatalog result1 = identifyByNameServiceCatalogService.execute(name1);
            ServiceCatalog result2 = identifyByNameServiceCatalogService.execute(name2);

            assertThat(result1.getName()).isEqualTo(name1);
            assertThat(result1.getDescription()).isEqualTo("Descrição 1");
            assertThat(result2.getName()).isEqualTo(name2);
            assertThat(result2.getDescription()).isEqualTo("Descrição 2");
        }

        @Test
        void shouldThrowNotFoundExceptionWithCorrectMessage() {
            String name = "Serviço inexistente";
            when(serviceCatalogRepository.findByName(name)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> identifyByNameServiceCatalogService.execute(name))
                    .isInstanceOf(ServiceCatalogNotFoundException.class)
                    .hasMessage("Serviço não encontrado com nome: " + name);
        }

        @Test
        void shouldFindServiceCatalogWithSpecialCharactersInName() {
            String nameWithSpecialChars = "Serviço de A/C - Limpeza";
            var serviceCatalog = createServiceCatalog(nameWithSpecialChars, "Limpeza do ar condicionado", new BigDecimal("250.00"));
            when(serviceCatalogRepository.findByName(nameWithSpecialChars)).thenReturn(Optional.of(serviceCatalog));

            ServiceCatalog result = identifyByNameServiceCatalogService.execute(nameWithSpecialChars);

            assertThat(result.getName()).isEqualTo(nameWithSpecialChars);
            verify(serviceCatalogRepository).findByName(nameWithSpecialChars);
        }
    }

    private ServiceCatalog createServiceCatalog(String name, String description, BigDecimal price) {
        return ServiceCatalog.reconstruct(
                UUID.randomUUID(),
                name,
                description,
                price,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}

