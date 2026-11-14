package br.com.ofisy.integration.messaging;

import br.com.ofisy.adapters.gateways.messaging.JpaOutboxEventRepository;
import br.com.ofisy.adapters.gateways.messaging.OutboxEventEntity;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import br.com.ofisy.domain.messaging.ProcessedEventRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import br.com.ofisy.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ServiceOrderEventsListenerIT extends IntegrationTestBase {

    @Autowired
    private SqsAsyncClient sqsAsyncClient;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private JpaOutboxEventRepository outboxEventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Value("${ofisy.messaging.queues.service-order}")
    private String queueName;

    @Test
    void shouldAcknowledgeEventWithoutHandler() {
        UUID eventId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        String body = """
                {
                  "eventId": "%s",
                  "type": "QuoteRejected",
                  "serviceOrderId": "%s",
                  "occurredAt": "2026-10-13T10:00:00Z",
                  "payload": { "quoteId": "%s" }
                }
                """.formatted(eventId, serviceOrderId, UUID.randomUUID());
        String queueUrl = queueUrl();

        send(queueUrl, serviceOrderId, body);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> assertThat(pendingMessages(queueUrl)).isZero());
        assertThat(processedEventRepository.existsById(eventId)).isFalse();
    }

    @Test
    void shouldStartExecutionAndAcknowledgeDuplicatedExecutionStarted() {
        UUID serviceOrderId = awaitingExecutionServiceOrder();
        UUID eventId = UUID.randomUUID();
        String body = envelope(eventId, "ExecutionStarted", serviceOrderId);
        String queueUrl = queueUrl();

        send(queueUrl, serviceOrderId, body);
        send(queueUrl, serviceOrderId, body);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> assertThat(pendingMessages(queueUrl)).isZero());
        assertThat(serviceOrderRepository.findById(serviceOrderId).orElseThrow().getStatus())
                .isEqualTo(ServiceOrderStatus.IN_PROGRESS);
        assertThat(processedEventRepository.existsById(eventId)).isTrue();
    }

    @Test
    void shouldFinishServiceOrderWhenAllExecutionsCompleted() {
        UUID serviceOrderId = awaitingExecutionServiceOrder();
        UUID startedEventId = UUID.randomUUID();
        UUID completedEventId = UUID.randomUUID();
        String queueUrl = queueUrl();

        send(queueUrl, serviceOrderId, envelope(startedEventId, "ExecutionStarted", serviceOrderId));
        send(queueUrl, serviceOrderId, envelope(completedEventId, "AllExecutionsCompleted", serviceOrderId));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> assertThat(pendingMessages(queueUrl)).isZero());
        ServiceOrder serviceOrder = serviceOrderRepository.findById(serviceOrderId).orElseThrow();
        assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.FINISHED);
        assertThat(serviceOrder.getFinishedAt()).isNotNull();
        assertThat(processedEventRepository.existsById(completedEventId)).isTrue();
        assertThat(outboxEventsOf(serviceOrderId, "ServiceOrderFinished")).hasSize(1);
    }

    @Test
    void shouldMoveServiceOrderToAwaitingApprovalWhenQuoteSentForApprovalIsDeliveredTwice() {
        UUID serviceOrderId = inDiagnosticServiceOrder();
        UUID eventId = UUID.randomUUID();
        String body = envelope(eventId, "QuoteSentForApproval", serviceOrderId);
        String queueUrl = queueUrl();

        send(queueUrl, serviceOrderId, body);
        send(queueUrl, serviceOrderId, body);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> assertThat(pendingMessages(queueUrl)).isZero());
        assertThat(serviceOrderRepository.findById(serviceOrderId).orElseThrow().getStatus())
                .isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
        assertThat(processedEventRepository.existsById(eventId)).isTrue();
    }

    @Test
    void shouldMoveServiceOrderToAwaitingExecutionWhenQuoteApproved() {
        UUID serviceOrderId = awaitingApprovalServiceOrder();
        UUID eventId = UUID.randomUUID();
        String queueUrl = queueUrl();

        send(queueUrl, serviceOrderId, envelope(eventId, "QuoteApproved", serviceOrderId));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> assertThat(pendingMessages(queueUrl)).isZero());
        assertThat(serviceOrderRepository.findById(serviceOrderId).orElseThrow().getStatus())
                .isEqualTo(ServiceOrderStatus.AWAITING_EXECUTION);
        assertThat(processedEventRepository.existsById(eventId)).isTrue();
    }

    @Test
    void shouldCancelServiceOrderAndPublishServiceOrderCancelledWhenStockRefunded() {
        UUID serviceOrderId = awaitingApprovalServiceOrder();
        UUID eventId = UUID.randomUUID();
        String body = envelope(eventId, "StockRefunded", serviceOrderId);
        String queueUrl = queueUrl();

        send(queueUrl, serviceOrderId, body);
        send(queueUrl, serviceOrderId, body);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> assertThat(pendingMessages(queueUrl)).isZero());
        assertThat(serviceOrderRepository.findById(serviceOrderId).orElseThrow().getStatus())
                .isEqualTo(ServiceOrderStatus.CANCELLED);
        assertThat(processedEventRepository.existsById(eventId)).isTrue();
        assertThat(outboxEventsOf(serviceOrderId, "ServiceOrderCancelled")).hasSize(1);
    }

    @Test
    void shouldCancelServiceOrderWhenQuoteCancelled() {
        UUID serviceOrderId = inDiagnosticServiceOrder();
        UUID eventId = UUID.randomUUID();
        String queueUrl = queueUrl();

        send(queueUrl, serviceOrderId, envelope(eventId, "QuoteCancelled", serviceOrderId));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> assertThat(pendingMessages(queueUrl)).isZero());
        assertThat(serviceOrderRepository.findById(serviceOrderId).orElseThrow().getStatus())
                .isEqualTo(ServiceOrderStatus.CANCELLED);
        assertThat(outboxEventsOf(serviceOrderId, "ServiceOrderCancelled")).hasSize(1);
    }

    private UUID inDiagnosticServiceOrder() {
        ServiceOrder serviceOrder = newServiceOrder();
        serviceOrder.startDiagnostic();
        return serviceOrderRepository.save(serviceOrder).getId();
    }

    private UUID awaitingApprovalServiceOrder() {
        ServiceOrder serviceOrder = newServiceOrder();
        serviceOrder.startDiagnostic();
        serviceOrder.sendToApproval();
        return serviceOrderRepository.save(serviceOrder).getId();
    }

    private UUID awaitingExecutionServiceOrder() {
        ServiceOrder serviceOrder = newServiceOrder();
        serviceOrder.startDiagnostic();
        serviceOrder.sendToApproval();
        serviceOrder.approve();
        return serviceOrderRepository.save(serviceOrder).getId();
    }

    private ServiceOrder newServiceOrder() {
        User user = userRepository.save(
                User.create("mecanico.listener.it@ofisy.com", passwordEncoder.encode("Test@123"), "Mecânico Listener IT", Role.MECHANIC));
        Customer customer = customerRepository.save(
                Customer.create(new CpfCnpj("52998224725"), "João Listener IT", "joao.listener.it@ofisy.com", "11992222222"));
        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(customer.getId(), new LicensePlate("LST1T01"), "Civic", "Honda", "Preto", 2022, null));

        return ServiceOrder.receive(vehicle.getId(), customer.getId(), "Relatório", user.getId());
    }

    private List<OutboxEventEntity> outboxEventsOf(UUID serviceOrderId, String eventType) {
        return outboxEventRepository.findAll().stream()
                .filter(event -> event.getServiceOrderId().equals(serviceOrderId))
                .filter(event -> event.getEventType().equals(eventType))
                .toList();
    }

    private String envelope(UUID eventId, String type, UUID serviceOrderId) {
        return """
                {
                  "eventId": "%s",
                  "type": "%s",
                  "serviceOrderId": "%s",
                  "occurredAt": "2026-10-20T10:00:00Z",
                  "payload": {}
                }
                """.formatted(eventId, type, serviceOrderId);
    }

    private String queueUrl() {
        return sqsAsyncClient.getQueueUrl(r -> r.queueName(queueName)).join().queueUrl();
    }

    private void send(String queueUrl, UUID serviceOrderId, String body) {
        sqsAsyncClient.sendMessage(r -> r
                .queueUrl(queueUrl)
                .messageGroupId(serviceOrderId.toString())
                .messageDeduplicationId(UUID.randomUUID().toString())
                .messageBody(body)).join();
    }

    private int pendingMessages(String queueUrl) {
        Map<QueueAttributeName, String> attributes = sqsAsyncClient.getQueueAttributes(r -> r
                .queueUrl(queueUrl)
                .attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
                        QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE))
                .join()
                .attributes();
        return attributes.values().stream().mapToInt(Integer::parseInt).sum();
    }
}
