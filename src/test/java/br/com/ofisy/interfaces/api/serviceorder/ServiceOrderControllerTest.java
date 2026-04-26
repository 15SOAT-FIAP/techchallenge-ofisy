package br.com.ofisy.interfaces.api.serviceorder;

import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.serviceorder.ServiceOrderService;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderResponseDTO;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
import br.com.ofisy.interfaces.api.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ServiceOrderControllerTest {

    private static final String BASE_URL = "/api/v1/service-orders";
    private static final String MOCK_USER_EMAIL = "attendant@ofisy.com";
    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Mock
    private ServiceOrderService serviceOrderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ServiceOrderController(serviceOrderService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        var mockUser = User.withUsername(MOCK_USER_EMAIL).password("").roles("ATTENDANT").build();
        var auth = new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class ReceiveServiceOrder {

        @Test
        void shouldReturn201WithCreatedServiceOrder() throws Exception {
            when(serviceOrderService.createServiceOrder(any(), any())).thenReturn(mockResponse());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("RECEIVED"))
                    .andExpect(jsonPath("$.vehicleId").value(VALID_VEHICLE_ID.toString()))
                    .andExpect(jsonPath("$.customerId").value(VALID_CUSTOMER_ID.toString()));
        }

        @Test
        void shouldReturn400WhenBodyIsEmpty() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de validação"))
                    .andExpect(jsonPath("$.errors.vehicleId").exists())
                    .andExpect(jsonPath("$.errors.customerId").exists());
        }

        @Test
        void shouldReturn400WhenBodyIsMissing() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400WhenVehicleIdIsMissing() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyMissingVehicleId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de validação"))
                    .andExpect(jsonPath("$.errors.vehicleId").exists());
        }

        @Test
        void shouldReturn400WhenCustomerIdIsMissing() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyMissingCustomerId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de validação"))
                    .andExpect(jsonPath("$.errors.customerId").exists());
        }

        @Test
        void shouldReturn404WhenCustomerNotFound() throws Exception {
            when(serviceOrderService.createServiceOrder(any(), any()))
                    .thenThrow(new CustomerNotFoundException(VALID_CUSTOMER_ID));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Cliente não encontrado"));
        }

        @Test
        void shouldReturn404WhenVehicleNotFound() throws Exception {
            when(serviceOrderService.createServiceOrder(any(), any()))
                    .thenThrow(new VehicleNotFoundException(VALID_VEHICLE_ID));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Veículo não encontrado"));
        }

        @Test
        void shouldReturn422WhenVehicleNotOwnedByCustomer() throws Exception {
            when(serviceOrderService.createServiceOrder(any(), any()))
                    .thenThrow(new VehicleNotOwnedByCustomerException(VALID_VEHICLE_ID, VALID_CUSTOMER_ID));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.title").value("Veículo não pertence ao cliente"));
        }
    }

    @Nested
    class StartDiagnosticServiceOrder {

        private static final UUID ORDER_ID = UUID.randomUUID();

        @Test
        void shouldReturn200WithUpdatedServiceOrder() throws Exception {
            when(serviceOrderService.startDiagnosticServiceOrder(ORDER_ID)).thenReturn(mockInDiagnosticResponse());

            mockMvc.perform(patch(BASE_URL + "/{id}/start-diagnostic", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_DIAGNOSTIC"));
        }

        @Test
        void shouldReturn404WhenOrderDoesNotExist() throws Exception {
            when(serviceOrderService.startDiagnosticServiceOrder(ORDER_ID))
                    .thenThrow(new ServiceOrderNotFoundException(ORDER_ID));

            mockMvc.perform(patch(BASE_URL + "/{id}/start-diagnostic", ORDER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Ordem de serviço não encontrada"));
        }

        @Test
        void shouldReturn409WhenTransitionIsInvalid() throws Exception {
            when(serviceOrderService.startDiagnosticServiceOrder(ORDER_ID))
                    .thenThrow(new InvalidServiceOrderTransitionException(ServiceOrderStatus.IN_DIAGNOSTIC, ServiceOrderStatus.IN_DIAGNOSTIC));

            mockMvc.perform(patch(BASE_URL + "/{id}/start-diagnostic", ORDER_ID))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Transição de status inválida"));
        }

        private ServiceOrderResponseDTO mockInDiagnosticResponse() {
            return new ServiceOrderResponseDTO(
                    ORDER_ID, VALID_VEHICLE_ID, VALID_CUSTOMER_ID,
                    "Barulho na suspensão", "IN_DIAGNOSTIC", UUID.randomUUID(), NOW, null, NOW);
        }
    }

    private String validBody() {
        return """
                {
                    "vehicleId": "%s",
                    "customerId": "%s",
                    "report": "Barulho na suspensão"
                }
                """.formatted(VALID_VEHICLE_ID, VALID_CUSTOMER_ID);
    }

    private String bodyMissingVehicleId() {
        return """
                {
                    "customerId": "%s"
                }
                """.formatted(VALID_CUSTOMER_ID);
    }

    private String bodyMissingCustomerId() {
        return """
                {
                    "vehicleId": "%s"
                }
                """.formatted(VALID_VEHICLE_ID);
    }

    private ServiceOrderResponseDTO mockResponse() {
        return new ServiceOrderResponseDTO(
                UUID.randomUUID(), VALID_VEHICLE_ID, VALID_CUSTOMER_ID,
                "Barulho na suspensão", "RECEIVED", UUID.randomUUID(), NOW, null, NOW);
    }
}