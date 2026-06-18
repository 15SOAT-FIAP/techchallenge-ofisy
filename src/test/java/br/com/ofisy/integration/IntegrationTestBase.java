package br.com.ofisy.integration;

import br.com.ofisy.adapters.gateways.customer.JpaCustomerRepository;
import br.com.ofisy.adapters.gateways.notification.JpaNotificationRepository;
import br.com.ofisy.adapters.gateways.quote.JpaQuoteRepository;
import br.com.ofisy.adapters.gateways.servicecatalog.JpaServiceCatalogRepository;
import br.com.ofisy.adapters.gateways.serviceorderexecution.JpaServiceOrderExecutionRepository;
import br.com.ofisy.adapters.gateways.stock.JpaStockRepository;
import br.com.ofisy.adapters.gateways.serviceorder.JpaServiceOrderRepository;
import br.com.ofisy.adapters.gateways.user.JpaUserRepository;
import br.com.ofisy.adapters.gateways.stockmovement.JpaStockMovementRepository;
import br.com.ofisy.adapters.gateways.vehicle.JpaVehicleRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "jwt.secret=test-secret-key-minimum-256-bits-long-for-hs256-algorithm-ok",
                "jwt.expiration=86400000",
                "spring.docker.compose.enabled=false"
        }
)
public abstract class IntegrationTestBase {

    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired private
    JpaNotificationRepository notificationRepository;
    @Autowired
    private JpaQuoteRepository quoteRepository;
    @Autowired
    private JpaServiceOrderExecutionRepository serviceOrderExecutionRepository;
    @Autowired
    private JpaServiceOrderRepository serviceOrderRepository;
    @Autowired
    private JpaStockMovementRepository stockMovementRepository;
    @Autowired
    protected JpaStockRepository stockRepository;
    @Autowired
    private JpaServiceCatalogRepository serviceCatalogRepository;
    @Autowired
    private JpaVehicleRepository vehicleRepository;
    @Autowired
    private JpaCustomerRepository customerRepository;
    @Autowired
    private JpaUserRepository userRepository;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterEach
    void cleanDatabase() {
        notificationRepository.deleteAll();
        quoteRepository.deleteAll();
        serviceOrderExecutionRepository.deleteAll();
        serviceOrderRepository.deleteAll();
        stockMovementRepository.deleteAll();
        stockRepository.deleteAll();
        serviceCatalogRepository.deleteAll();
        vehicleRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected String obtainToken(String email, String rawPassword) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", rawPassword))
                .when()
                .post("/api/v1/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }
}