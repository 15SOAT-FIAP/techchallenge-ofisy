package br.com.ofisy.infra.notification.publish;

import br.com.ofisy.application.notification.publish.NotificationEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SqsNotificationEventPublisherTest {

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldPublishLowStock() throws Exception {
        SqsNotificationEventPublisher publisher = new SqsNotificationEventPublisher(sqsTemplate, new ObjectMapper(), "queue");
        publisher.publishLowStock(new NotificationEventPublisher.LowStockEvent(UUID.randomUUID(), "Product", 10, 5));
        verify(sqsTemplate).send(eq("queue"), anyString());
    }

    @Test
    void shouldPublishQuoteGenerated() throws Exception {
        SqsNotificationEventPublisher publisher = new SqsNotificationEventPublisher(sqsTemplate, new ObjectMapper(), "queue");
        publisher.publishQuoteGenerated(new NotificationEventPublisher.QuoteGeneratedEvent(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100")));
        verify(sqsTemplate).send(eq("queue"), anyString());
    }
}
