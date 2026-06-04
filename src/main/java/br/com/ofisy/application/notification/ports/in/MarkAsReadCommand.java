package br.com.ofisy.application.notification.ports.in;

import java.util.Objects;
import java.util.UUID;

public record MarkAsReadCommand(UUID notificationId) {
    public MarkAsReadCommand {
        if (notificationId == null) throw new IllegalArgumentException("notificationId não pode ser nulo");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarkAsReadCommand that = (MarkAsReadCommand) o;
        return Objects.equals(notificationId, that.notificationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId);
    }
}
