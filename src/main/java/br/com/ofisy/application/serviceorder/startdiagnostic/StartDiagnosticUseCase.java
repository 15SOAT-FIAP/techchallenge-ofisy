package br.com.ofisy.application.serviceorder.startdiagnostic;

import br.com.ofisy.domain.serviceorder.ServiceOrder;

import java.util.UUID;

public interface StartDiagnosticUseCase {

    ServiceOrder execute(UUID id);
}