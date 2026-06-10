package br.com.ofisy.application.serviceorder.create;

import br.com.ofisy.application.customer.identifybyid.IdentifyByIdCustomerUseCase;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.user.UserService;
import br.com.ofisy.application.vehicle.identifybyid.IdentifyVehicleByIdUseCase;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.vehicle.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateServiceOrderService implements CreateServiceOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final IdentifyByIdCustomerUseCase identifyByIdCustomerUseCase;
    private final IdentifyVehicleByIdUseCase identifyVehicleByIdUseCase;
    private final UserService userService;

    @Override
    @Transactional
    public ServiceOrder execute(CreateServiceOrderCommand cmd) {
        identifyByIdCustomerUseCase.execute(cmd.customerId());
        Vehicle vehicle = identifyVehicleByIdUseCase.execute(cmd.vehicleId());
        if (!vehicle.getCustomerId().equals(cmd.customerId())) {
            throw new VehicleNotOwnedByCustomerException(cmd.vehicleId(), cmd.customerId());
        }
        UUID createdBy = userService.getIdByEmail(cmd.createdByEmail());
        ServiceOrder serviceOrder = ServiceOrder.receive(cmd.vehicleId(), cmd.customerId(), cmd.report(), createdBy);
        return serviceOrderRepository.save(serviceOrder);
    }
}