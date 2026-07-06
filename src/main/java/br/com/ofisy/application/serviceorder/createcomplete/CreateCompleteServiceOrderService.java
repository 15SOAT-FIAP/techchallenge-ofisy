package br.com.ofisy.application.serviceorder.createcomplete;

import br.com.ofisy.application.customer.identifybyid.IdentifyByIdCustomerUseCase;
import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.application.serviceorder.create.CreateServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.user.getidbyemail.GetIdByEmailUseCase;
import br.com.ofisy.application.vehicle.identifybyid.IdentifyVehicleByIdUseCase;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.vehicle.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCompleteServiceOrderService implements CreateCompleteServiceOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final IdentifyByIdCustomerUseCase identifyByIdCustomerUseCase;
    private final IdentifyVehicleByIdUseCase identifyVehicleByIdUseCase;
    private final GetIdByEmailUseCase getIdByEmailUseCase;
    private final CreateQuoteUseCase createQuoteUseCase;

    @Override
    @Transactional
    public ServiceOrder execute(CreateCompleteServiceOrderUseCase.CreateCompleteServiceOrderCommand cmd) {
        identifyByIdCustomerUseCase.execute(cmd.customerId());

        Vehicle vehicle = identifyVehicleByIdUseCase.execute(cmd.vehicleId());
        if (!vehicle.getCustomerId().equals(cmd.customerId())) {
            throw new VehicleNotOwnedByCustomerException(cmd.vehicleId(), cmd.customerId());
        }

        UUID createdBy = getIdByEmailUseCase.execute(cmd.createdByEmail());
        ServiceOrder serviceOrder = ServiceOrder.receive(cmd.vehicleId(), cmd.customerId(), cmd.report(), createdBy);

        serviceOrder = serviceOrderRepository.save(serviceOrder);

        boolean hasItems = hasItems(cmd.stockItems(), cmd.serviceItems());
        if (hasItems) {

            createQuoteUseCase.execute(new CreateQuoteUseCase.CreateQuoteCommand(
                    serviceOrder.getId(),
                    cmd.stockItems() != null ? cmd.stockItems() : List.of(),
                    cmd.serviceItems() != null ? cmd.serviceItems() : List.of()
            ));
        }

        return serviceOrder;
    }

    private boolean hasItems(
            List<CreateQuoteUseCase.StockItemCommand> stockItems,
            List<CreateQuoteUseCase.ServiceItemCommand> serviceItems) {
        boolean hasStock = stockItems != null && !stockItems.isEmpty();
        boolean hasService = serviceItems != null && !serviceItems.isEmpty();
        return hasStock || hasService;
    }
}