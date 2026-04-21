package br.com.ofisy.application.vehicle.exceptions;

public class VehicleLicensePlateNotFoundException extends RuntimeException {
    public VehicleLicensePlateNotFoundException(String licensePlate) {
        super("Veículo com placa '" + licensePlate + "' não encontrado");
    }
}
