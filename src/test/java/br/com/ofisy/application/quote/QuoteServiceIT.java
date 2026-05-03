package br.com.ofisy.application.quote;

import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.application.serviceorder.ServiceOrderService;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.application.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.StockItemRequestDTO;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import br.com.ofisy.integration.IntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuoteServiceIT extends IntegrationTestBase {

    @Autowired
    private QuoteService quoteService;
    @Autowired
    private ServiceOrderService serviceOrderService;

    @Autowired 
    private CustomerRepository customerDomainRepository;
    @Autowired 
    private VehicleRepository vehicleDomainRepository;
    @Autowired 
    private UserRepository userDomainRepository;
    @Autowired 
    private StockRepository stockDomainRepository;
    @Autowired 
    private ServiceCatalogRepository serviceCatalogDomainRepository;

    private UUID customerId;
    private UUID vehicleId;
    private UUID stockId;
    private String userEmail;

    @BeforeEach
    void setUp() {
        User user = userDomainRepository.save(
                User.create("mecanico.quote.it@ofisy.com", passwordEncoder.encode("Test@123"), "Mecânico Quote IT", Role.MECHANIC)
        );
        userEmail = user.getEmail().emailAddress();

        Customer customer = customerDomainRepository.save(
                Customer.create(new CpfCnpj("98765432100"), "Cliente Quote IT", "cliente.quote.it@ofisy.com", "11993333333")
        );
        customerId = customer.getId();

        Vehicle vehicle = vehicleDomainRepository.save(
                Vehicle.create(customerId, new LicensePlate("QTE1T01"), "Uno", "Fiat", "Branco", 2020, null)
        );
        vehicleId = vehicle.getId();

        Stock stock = stockDomainRepository.save(
                Stock.create("Peça Quote IT", "Peça para testes de quote service", 10, new BigDecimal("150.00"), "Testes", 2)
        );
        stockId = stock.getId();
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar orçamento por ID")
        void shouldReturnQuoteById() {
            var serviceOrderId = createAndStartDiagnostic();
            var quote = serviceOrderService.generateQuote(serviceOrderId, quoteRequest(serviceOrderId, 1));

            var response = quoteService.findById(quote.id());

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(quote.id());
            assertThat(response.serviceOrderId()).isEqualTo(serviceOrderId);
        }

        @Test
        @DisplayName("Deve lançar exceção quando orçamento não encontrado")
        void shouldThrowExceptionWhenQuoteNotFound() {
            assertThatThrownBy(() -> quoteService.findById(UUID.randomUUID()))
                    .isInstanceOf(QuoteNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findByServiceOrderId")
    class FindByServiceOrderId {

        @Test
        @DisplayName("Deve retornar orçamentos por OS")
        void shouldReturnQuotesByServiceOrderId() {
            var serviceOrderId = createAndStartDiagnostic();
            serviceOrderService.generateQuote(serviceOrderId, quoteRequest(serviceOrderId, 1));

            var response = quoteService.findByServiceOrderId(serviceOrderId);

            assertThat(response).hasSize(1);
            assertThat(response.getFirst().serviceOrderId()).isEqualTo(serviceOrderId);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há orçamentos para a OS")
        void shouldReturnEmptyListWhenNoQuotes() {
            var response = quoteService.findByServiceOrderId(UUID.randomUUID());

            assertThat(response).isEmpty();
        }
    }

    private UUID createAndStartDiagnostic() {
        var created = serviceOrderService.create(
                new ServiceOrderRequestDTO(vehicleId, customerId, "Relatório Quote IT"), userEmail
        );
        serviceOrderService.startDiagnostic(created.id());
        return created.id();
    }

    private CreateQuoteRequestDTO quoteRequest(UUID serviceOrderId, int quantity) {
        return new CreateQuoteRequestDTO(serviceOrderId,
                List.of(new StockItemRequestDTO(stockId, quantity)),
                List.of());
    }
}