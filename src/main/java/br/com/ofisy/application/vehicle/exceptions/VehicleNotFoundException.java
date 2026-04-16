package br.com.ofisy.application.vehicle.exceptions;

import java.util.UUID;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(UUID id) {
        super("Veículo com ID " + id + " não encontrado");
    }
}
