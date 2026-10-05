package br.com.ofisy.infra.notification.publish;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SqsDummyConfig {

    @Bean
    @Primary
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory() {
        return new SqsMessageListenerContainerFactory<>();
    }
}
