package br.com.ofisy.application.serviceorder.exceptions;

import java.util.UUID;

public class VehicleNotOwnedByCustomerException extends RuntimeException {
    public VehicleNotOwnedByCustomerException(UUID vehicleId, UUID customerId) {
        super("Veiculo com ID " + vehicleId + " não pertence ao cliente com ID " + customerId);
    }
}
