package br.com.ofisy.application.serviceorder;

import br.com.ofisy.application.customer.CustomerService;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderResponseDTO;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.user.UserService;
import br.com.ofisy.application.vehicle.VehicleService;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final UserService userService;

    @Transactional
    public ServiceOrderResponseDTO createServiceOrder(ServiceOrderRequestDTO request, String createdByEmail) {
        customerService.identifyCustomerById(request.customerId());
        var vehicle = vehicleService.identifyVehicleById(request.vehicleId());
        if (!vehicle.customerId().equals(request.customerId())) {
            throw new VehicleNotOwnedByCustomerException(request.vehicleId(), request.customerId());
        }

        var createdBy = userService.getIdByEmail(createdByEmail);
        var serviceOrder = ServiceOrderMapper.toDomain(request, createdBy);
        var receivedServiceOrder = serviceOrderRepository.save(serviceOrder);
        return ServiceOrderMapper.toResponseDTO(receivedServiceOrder);
    }
}
