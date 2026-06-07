package br.com.ofisy.domain.notification.exceptions;

public class InvalidNotificationMessageException extends RuntimeException {
    public InvalidNotificationMessageException(String message) {
        super(message);
    }
}
