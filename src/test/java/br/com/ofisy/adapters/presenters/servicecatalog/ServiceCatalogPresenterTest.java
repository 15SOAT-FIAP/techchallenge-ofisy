package br.com.ofisy.adapters.presenters.servicecatalog;

import br.com.ofisy.adapters.controllers.servicecatalog.dto.ServiceCatalogResponseDTO;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceCatalogPresenterTest {

    private static final UUID VALID_ID = UUID.randomUUID();
    private static final String VALID_NAME = "Troca de Óleo";
    private static final String VALID_DESCRIPTION = "Troca de óleo do motor";
    private static final BigDecimal VALID_PRICE = new BigDecimal("50.00");
    private static final LocalDateTime NOW = LocalDateTime.of(2024, 1, 10, 10, 0);

    @Test
    void shouldMapAllFieldsFromDomainToResponseDTO() {
        var serviceCatalog = ServiceCatalog.reconstruct(VALID_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, NOW, NOW);

        var result = ServiceCatalogPresenter.present(serviceCatalog);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(VALID_ID);
        assertThat(result.name()).isEqualTo(VALID_NAME);
        assertThat(result.description()).isEqualTo(VALID_DESCRIPTION);
        assertThat(result.price()).isEqualTo(VALID_PRICE);
        assertThat(result.createdAt()).isEqualTo(NOW);
        assertThat(result.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldPreserveUUIDFromDomain() {
        var serviceCatalog = ServiceCatalog.reconstruct(VALID_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, NOW, NOW);

        var result = ServiceCatalogPresenter.present(serviceCatalog);

        assertThat(result.id()).isEqualTo(VALID_ID);
    }

    @Test
    void shouldPreserveNameFromDomain() {
        var serviceCatalog = ServiceCatalog.reconstruct(VALID_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, NOW, NOW);

        var result = ServiceCatalogPresenter.present(serviceCatalog);

        assertThat(result.name()).isEqualTo(VALID_NAME);
    }

    @Test
    void shouldPreserveDescriptionFromDomain() {
        var serviceCatalog = ServiceCatalog.reconstruct(VALID_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, NOW, NOW);

        var result = ServiceCatalogPresenter.present(serviceCatalog);

        assertThat(result.description()).isEqualTo(VALID_DESCRIPTION);
    }

    @Test
    void shouldPreservePriceFromDomain() {
        var serviceCatalog = ServiceCatalog.reconstruct(VALID_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, NOW, NOW);

        var result = ServiceCatalogPresenter.present(serviceCatalog);

        assertThat(result.price()).isEqualTo(VALID_PRICE);
    }

    @Test
    void shouldPreserveTimestamps() {
        var createdAt = LocalDateTime.of(2024, 1, 10, 10, 0);
        var updatedAt = LocalDateTime.of(2024, 2, 1, 18, 30);
        var serviceCatalog = ServiceCatalog.reconstruct(VALID_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, createdAt, updatedAt);

        var result = ServiceCatalogPresenter.present(serviceCatalog);

        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void shouldConvertToResponseDTORecord() {
        var serviceCatalog = ServiceCatalog.reconstruct(VALID_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, NOW, NOW);

        var result = ServiceCatalogPresenter.present(serviceCatalog);

        assertThat(result).isInstanceOf(ServiceCatalogResponseDTO.class);
    }

    @Test
    void shouldHandleDifferentPriceValues() {
        var price = new BigDecimal("99.99");
        var serviceCatalog = ServiceCatalog.reconstruct(VALID_ID, VALID_NAME, VALID_DESCRIPTION, price, NOW, NOW);

        var result = ServiceCatalogPresenter.present(serviceCatalog);

        assertThat(result.price()).isEqualTo(price);
        assertThat(result.price()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    void shouldHandleSmallPriceValues() {
        var price = new BigDecimal("0.01");
        var serviceCatalog = ServiceCatalog.reconstruct(VALID_ID, VALID_NAME, VALID_DESCRIPTION, price, NOW, NOW);

        var result = ServiceCatalogPresenter.present(serviceCatalog);

        assertThat(result.price()).isEqualTo(price);
    }

    @Test
    void shouldHandleLargePriceValues() {
        var price = new BigDecimal("9999.99");
        var serviceCatalog = ServiceCatalog.reconstruct(VALID_ID, VALID_NAME, VALID_DESCRIPTION, price, NOW, NOW);

        var result = ServiceCatalogPresenter.present(serviceCatalog);

        assertThat(result.price()).isEqualTo(price);
    }

    @Test
    void shouldHandleVariousServiceNames() {
        var serviceName = "Alinhamento de Rodas";
        var serviceCatalog = ServiceCatalog.reconstruct(VALID_ID, serviceName, VALID_DESCRIPTION, VALID_PRICE, NOW, NOW);

        var result = ServiceCatalogPresenter.present(serviceCatalog);

        assertThat(result.name()).isEqualTo(serviceName);
    }

    @Test
    void shouldHandleVariousServiceDescriptions() {
        var description = "Realiza o alinhamento preciso das rodas do veículo";
        var serviceCatalog = ServiceCatalog.reconstruct(VALID_ID, VALID_NAME, description, VALID_PRICE, NOW, NOW);

        var result = ServiceCatalogPresenter.present(serviceCatalog);

        assertThat(result.description()).isEqualTo(description);
    }

    @Test
    void shouldHandleMultipleDifferentServices() {
        var service1Id = UUID.randomUUID();
        var service1 = ServiceCatalog.reconstruct(service1Id, "Serviço 1", "Descrição 1", new BigDecimal("50.00"), NOW, NOW);

        var service2Id = UUID.randomUUID();
        var service2 = ServiceCatalog.reconstruct(service2Id, "Serviço 2", "Descrição 2", new BigDecimal("100.00"), NOW, NOW);

        var result1 = ServiceCatalogPresenter.present(service1);
        var result2 = ServiceCatalogPresenter.present(service2);

        assertThat(result1.id()).isEqualTo(service1Id);
        assertThat(result2.id()).isEqualTo(service2Id);
        assertThat(result1.name()).isEqualTo("Serviço 1");
        assertThat(result2.name()).isEqualTo("Serviço 2");
        assertThat(result1.price()).isEqualTo(new BigDecimal("50.00"));
        assertThat(result2.price()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldReturnCorrectResponseDTOStructure() {
        var serviceCatalog = ServiceCatalog.reconstruct(VALID_ID, VALID_NAME, VALID_DESCRIPTION, VALID_PRICE, NOW, NOW);

        var result = ServiceCatalogPresenter.present(serviceCatalog);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", VALID_ID)
                .hasFieldOrPropertyWithValue("name", VALID_NAME)
                .hasFieldOrPropertyWithValue("description", VALID_DESCRIPTION)
                .hasFieldOrPropertyWithValue("price", VALID_PRICE)
                .hasFieldOrPropertyWithValue("createdAt", NOW)
                .hasFieldOrPropertyWithValue("updatedAt", NOW);
    }
}
