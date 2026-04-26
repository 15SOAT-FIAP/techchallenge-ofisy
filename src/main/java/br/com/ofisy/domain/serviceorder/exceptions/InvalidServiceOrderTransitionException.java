package br.com.ofisy.domain.serviceorder.exceptions;

import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;

public class InvalidServiceOrderTransitionException extends RuntimeException {

    public InvalidServiceOrderTransitionException(ServiceOrderStatus from, ServiceOrderStatus to) {
        super("Nao pode alterar o status da ordem de servico de " + from + " para " + to);
    }
}