package br.com.ofisy.integration;

import br.com.ofisy.adapters.gateways.customer.JpaCustomerRepository;
import br.com.ofisy.adapters.gateways.messaging.JpaOutboxEventRepository;
import br.com.ofisy.adapters.gateways.messaging.JpaProcessedEventRepository;
import br.com.ofisy.adapters.gateways.servicecatalog.JpaServiceCatalogRepository;
import br.com.ofisy.adapters.gateways.serviceorder.JpaServiceOrderRepository;
import br.com.ofisy.adapters.gateways.user.JpaUserRepository;
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
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

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

    static final LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:4.13.1"))
            .withServices("sns", "sqs")
            .withCopyFileToContainer(
                    MountableFile.forHostPath("localstack/init/ready.d/01-messaging.sh", 0755),
                    "/etc/localstack/init/ready.d/01-messaging.sh")
            .waitingFor(Wait.forLogMessage("^Ready\\.\\s*$", 1));

    static {
        postgres.start();
        localstack.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.cloud.aws.endpoint", () -> localstack.getEndpoint().toString());
        registry.add("spring.cloud.aws.region.static", localstack::getRegion);
        registry.add("spring.cloud.aws.credentials.access-key", localstack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localstack::getSecretKey);
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired 
    private JpaServiceOrderRepository serviceOrderRepository;
    @Autowired 
    private JpaServiceCatalogRepository serviceCatalogRepository;
    @Autowired 
    private JpaVehicleRepository vehicleRepository;
    @Autowired 
    private JpaCustomerRepository customerRepository;
    @Autowired
    private JpaUserRepository userRepository;
    @Autowired
    private JpaOutboxEventRepository outboxEventRepository;
    @Autowired
    private JpaProcessedEventRepository processedEventRepository;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterEach
    void cleanDatabase() {
        serviceOrderRepository.deleteAll();
        serviceCatalogRepository.deleteAll();
        vehicleRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
        outboxEventRepository.deleteAllInBatch();
        processedEventRepository.deleteAllInBatch();
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