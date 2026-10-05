package br.com.ofisy.infra.notification.publish;

import br.com.ofisy.application.notification.publish.NotificationEventPublisher;
import tools.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SqsNotificationEventPublisher implements NotificationEventPublisher {

    private final SqsTemplate sqsTemplate;
    private final String queueName;
    private final ObjectMapper objectMapper;

    public SqsNotificationEventPublisher(
            SqsTemplate sqsTemplate,
            ObjectMapper objectMapper,
            @Value("${app.aws.sqs.notification-queue:techchallenge-ofisy-notifications-queue}") String queueName) {
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
        this.queueName = queueName;
    }

    @Override
    public void publishLowStock(LowStockEvent event) {
        try {
            String json = objectMapper.writeValueAsString(new EventWrapper("LOW_STOCK", event));
            sqsTemplate.send(queueName, json);
            log.info("Successfully published LowStockEvent for stockId={}", event.stockId());
        } catch (Exception e) {
            log.error("Failed to publish LowStockEvent for stockId={}. Best effort, ignoring error.", event.stockId(), e);
        }
    }

    @Override
    public void publishQuoteGenerated(QuoteGeneratedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(new EventWrapper("QUOTE_GENERATED", event));
            sqsTemplate.send(queueName, json);
            log.info("Successfully published QuoteGeneratedEvent for quoteId={}", event.quoteId());
        } catch (Exception e) {
            log.error("Failed to publish QuoteGeneratedEvent for quoteId={}. Best effort, ignoring error.", event.quoteId(), e);
        }
    }

    record EventWrapper(String eventType, Object payload) {}
}
