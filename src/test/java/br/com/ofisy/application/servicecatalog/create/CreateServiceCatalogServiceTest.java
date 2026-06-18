package br.com.ofisy.application.servicecatalog.create;

import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateServiceCatalogServiceTest {

    private static final String VALID_NAME = "Troca de óleo";
    private static final String VALID_DESCRIPTION = "Troca de óleo e filtro";
    private static final BigDecimal VALID_PRICE = new BigDecimal("150.00");

    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;

    @InjectMocks
    private CreateServiceCatalogService createServiceCatalogService;

    private CreateServiceCatalogUseCase.CreateServiceCatalogCommand validCommand() {
        return new CreateServiceCatalogUseCase.CreateServiceCatalogCommand(
                VALID_NAME, VALID_DESCRIPTION, VALID_PRICE);
    }

    @Nested
    class Execute {

        @Test
        void shouldCreateServiceCatalogSuccessfully() {
            var cmd = validCommand();
            when(serviceCatalogRepository.save(any())).thenAnswer(inv -> {
                ServiceCatalog serviceCatalog = inv.getArgument(0);
                return ServiceCatalog.reconstruct(
                        java.util.UUID.randomUUID(),
                        serviceCatalog.getName(),
                        serviceCatalog.getDescription(),
                        serviceCatalog.getPrice(),
                        serviceCatalog.getCreatedAt(),
                        serviceCatalog.getUpdatedAt()
                );
            });

            ServiceCatalog result = createServiceCatalogService.execute(cmd);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(VALID_NAME);
            assertThat(result.getDescription()).isEqualTo(VALID_DESCRIPTION);
            assertThat(result.getPrice()).isEqualTo(VALID_PRICE);
            verify(serviceCatalogRepository).save(any());
        }

        @Test
        void shouldCreateServiceCatalogWithAllFields() {
            var cmd = new CreateServiceCatalogUseCase.CreateServiceCatalogCommand(
                    "Revisão completa",
                    "Revisão completa do veículo",
                    new BigDecimal("500.00")
            );

            when(serviceCatalogRepository.save(any())).thenAnswer(inv -> {
                ServiceCatalog serviceCatalog = inv.getArgument(0);
                return ServiceCatalog.reconstruct(
                        java.util.UUID.randomUUID(),
                        serviceCatalog.getName(),
                        serviceCatalog.getDescription(),
                        serviceCatalog.getPrice(),
                        serviceCatalog.getCreatedAt(),
                        serviceCatalog.getUpdatedAt()
                );
            });

            ServiceCatalog result = createServiceCatalogService.execute(cmd);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Revisão completa");
            assertThat(result.getDescription()).isEqualTo("Revisão completa do veículo");
            assertThat(result.getPrice()).isEqualTo(new BigDecimal("500.00"));
            assertThat(result.getId()).isNotNull();
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();
            verify(serviceCatalogRepository).save(any());
        }

        @Test
        void shouldCreateServiceCatalogWithHighPrice() {
            var cmd = new CreateServiceCatalogUseCase.CreateServiceCatalogCommand(
                    "Serviço Premium",
                    "Serviço de alta qualidade",
                    new BigDecimal("999.99")
            );

            when(serviceCatalogRepository.save(any())).thenAnswer(inv -> {
                ServiceCatalog serviceCatalog = inv.getArgument(0);
                return ServiceCatalog.reconstruct(
                        java.util.UUID.randomUUID(),
                        serviceCatalog.getName(),
                        serviceCatalog.getDescription(),
                        serviceCatalog.getPrice(),
                        serviceCatalog.getCreatedAt(),
                        serviceCatalog.getUpdatedAt()
                );
            });

            ServiceCatalog result = createServiceCatalogService.execute(cmd);

            assertThat(result).isNotNull();
            assertThat(result.getPrice()).isEqualTo(new BigDecimal("999.99"));
        }
    }
}

