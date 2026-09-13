package br.com.ofisy.config.metrics;

import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class ServiceOrderMetrics {

    private final MeterRegistry registry;

    public ServiceOrderMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordCreated() {
        Counter.builder("serviceorder.status.transitions")
                .tag("from", "none")
                .tag("to", ServiceOrderStatus.RECEIVED.name())
                .register(registry)
                .increment();
    }

    public void recordTransition(ServiceOrderStatus from, ServiceOrderStatus to, LocalDateTime enteredFromStatusAt) {
        Counter.builder("serviceorder.status.transitions")
                .tag("from", from.name())
                .tag("to", to.name())
                .register(registry)
                .increment();

        Timer.builder("serviceorder.status.duration")
                .tag("status", from.name())
                .tag("to", to.name())
                .register(registry)
                .record(Duration.between(enteredFromStatusAt, LocalDateTime.now()));
    }
}
