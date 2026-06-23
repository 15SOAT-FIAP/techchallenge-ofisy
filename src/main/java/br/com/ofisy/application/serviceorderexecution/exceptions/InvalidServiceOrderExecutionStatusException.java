package br.com.ofisy.application.serviceorderexecution.exceptions;

public class InvalidServiceOrderExecutionStatusException extends RuntimeException {

    public InvalidServiceOrderExecutionStatusException(String status) {
        super("Status inválido: " + status);
    }
}