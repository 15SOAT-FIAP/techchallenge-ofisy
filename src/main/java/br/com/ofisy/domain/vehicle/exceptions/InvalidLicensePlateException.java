package br.com.ofisy.domain.vehicle.exceptions;

public class InvalidLicensePlateException extends RuntimeException {
    public InvalidLicensePlateException(String value) {
        super("Placa de veículo inválida: " + value);
    }
}
