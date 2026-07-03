package br.com.ofisy.adapters.controllers.serviceorder.dto;

import br.com.ofisy.adapters.controllers.quote.dto.ServiceItemRequestDTO;
import br.com.ofisy.adapters.controllers.quote.dto.StockItemRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateServiceOrderRequestDTO(
        @NotNull(message = "Veiculo é obrigatório")
        UUID vehicleId,
        @NotNull(message = "Cliente é obrigatório")
        UUID customerId,
        String report,
        @Valid
        List<StockItemRequestDTO> stockItems,
        @Valid
        List<ServiceItemRequestDTO> serviceItems
) {
}