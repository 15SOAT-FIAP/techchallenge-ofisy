package br.com.ofisy.application.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockNotificationSender {

    public void send(String message) {
        log.info("[MOCK NOTIFICATION SENT] → {}", message);
    }
}