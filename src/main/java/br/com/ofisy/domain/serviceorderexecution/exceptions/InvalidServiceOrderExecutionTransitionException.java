package br.com.ofisy.domain.serviceorderexecution.exceptions;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;

public class InvalidServiceOrderExecutionTransitionException extends RuntimeException {

    public InvalidServiceOrderExecutionTransitionException(ServiceOrderExecutionStatus from,
                                                           ServiceOrderExecutionStatus to) {
        super("Nao pode alterar o status da execucao de " + from + " para " + to);
    }
}