package br.com.ofisy.application.serviceorderexecution.getaverageexecutiontime;

import java.util.UUID;

public interface GetAverageExecutionTimeServiceOrderExecutionUseCase {
    double execute(UUID serviceCatalogId);
}

