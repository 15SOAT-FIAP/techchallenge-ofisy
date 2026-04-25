package br.com.ofisy.application.serviceorder.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ServiceOrderRequestDTO(
        @NotNull(message = "Veiculo é obrigatório")
        UUID vehicleId,
        @NotNull(message = "Cliente é obrigatório")
        UUID customerId,
        String report
) {
}
