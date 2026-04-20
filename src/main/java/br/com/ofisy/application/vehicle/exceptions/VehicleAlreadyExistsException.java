package br.com.ofisy.application.vehicle.exceptions;

public class VehicleAlreadyExistsException extends RuntimeException {
    public VehicleAlreadyExistsException(String licensePlate) {
        super("Veículo com placa " + licensePlate + " já está registrado.");
    }
}
