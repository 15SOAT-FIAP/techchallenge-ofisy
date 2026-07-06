package br.com.ofisy.application.quote;

import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.application.quote.findbyid.FindQuoteByIdUseCase;
import br.com.ofisy.application.quote.findbyserviceorderid.FindQuoteByServiceOrderIdUseCase;
import br.com.ofisy.application.serviceorder.create.CreateServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.startdiagnostic.StartDiagnosticUseCase;
import br.com.ofisy.application.serviceorder.generatequote.GenerateServiceOrderQuoteUseCase;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import br.com.ofisy.integration.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuoteServiceIT extends IntegrationTestBase {

    @Autowired private FindQuoteByIdUseCase findQuoteByIdUseCase;
    @Autowired private FindQuoteByServiceOrderIdUseCase findQuoteByServiceOrderIdUseCase;
    @Autowired private CreateServiceOrderUseCase createServiceOrderUseCase;
    @Autowired private StartDiagnosticUseCase startDiagnosticUseCase;
    @Autowired private GenerateServiceOrderQuoteUseCase generateServiceOrderQuoteUseCase;

    @Autowired private CustomerRepository customerDomainRepository;
    @Autowired private VehicleRepository vehicleDomainRepository;
    @Autowired private UserRepository userDomainRepository;
    @Autowired private StockRepository stockDomainRepository;

    private UUID customerId;
    private UUID vehicleId;
    private UUID stockId;
    private String userEmail;

    @BeforeEach
    void setUp() {
        User user = userDomainRepository.save(
                User.create("mecanico.quote.it@ofisy.com", passwordEncoder.encode("Test@123"), "Mecânico Quote IT", Role.MECHANIC));
        userEmail = user.getEmail().emailAddress();

        Customer customer = customerDomainRepository.save(
                Customer.create(new CpfCnpj("98765432100"), "Cliente Quote IT", "cliente.quote.it@ofisy.com", "11993333333"));
        customerId = customer.getId();

        Vehicle vehicle = vehicleDomainRepository.save(
                Vehicle.create(customerId, new LicensePlate("QTE1T01"), "Uno", "Fiat", "Branco", 2020, null));
        vehicleId = vehicle.getId();

        Stock stock = stockDomainRepository.save(
                Stock.create("Peça Quote IT", "Peça para testes de quote service", 10, new BigDecimal("150.00"), "Testes", 2));
        stockId = stock.getId();
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar orçamento por ID")
        void shouldReturnQuoteById() {
            var quote = createAndGenerateQuote();

            Quote response = findQuoteByIdUseCase.execute(quote.getId());

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(quote.getId());
        }

        @Test
        @DisplayName("Deve lançar exceção quando orçamento não encontrado")
        void shouldThrowExceptionWhenQuoteNotFound() {
            assertThatThrownBy(() -> findQuoteByIdUseCase.execute(UUID.randomUUID()))
                    .isInstanceOf(QuoteNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findByServiceOrderId")
    class FindByServiceOrderId {

        @Test
        @DisplayName("Deve retornar orçamentos por OS")
        void shouldReturnQuotesByServiceOrderId() {
            var quote = createAndGenerateQuote();

            List<Quote> response = findQuoteByServiceOrderIdUseCase.execute(quote.getServiceOrderId());

            assertThat(response).hasSize(1);
            assertThat(response.getFirst().getServiceOrderId()).isEqualTo(quote.getServiceOrderId());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há orçamentos para a OS")
        void shouldReturnEmptyListWhenNoQuotes() {
            List<Quote> response = findQuoteByServiceOrderIdUseCase.execute(UUID.randomUUID());

            assertThat(response).isEmpty();
        }
    }

    private Quote createAndGenerateQuote() {
        ServiceOrder serviceOrder = createServiceOrderUseCase.execute(
                new CreateServiceOrderUseCase.CreateServiceOrderCommand(
                        vehicleId, customerId, "Relatório Quote IT", userEmail));

        startDiagnosticUseCase.execute(serviceOrder.getId());

        return generateServiceOrderQuoteUseCase.execute(
                new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                        serviceOrder.getId(),
                        List.of(new CreateQuoteUseCase.StockItemCommand(stockId, 1)),
                        List.of()));
    }
}