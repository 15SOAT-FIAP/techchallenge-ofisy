package br.com.ofisy.adapters.controllers.serviceorder;

import br.com.ofisy.adapters.controllers.serviceorder.dto.ServiceOrderResponseDTO;
import br.com.ofisy.adapters.controllers.serviceorder.dto.ServiceOrderStatusResponseDTO;
import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.quote.dto.QuoteResponseDTO;
import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.application.serviceorder.ServiceOrderService;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver())
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
            when(serviceOrderService.create(any(), any(), any(), any())).thenReturn(mockServiceOrder(ServiceOrderStatus.RECEIVED));

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
            when(serviceOrderService.create(any(), any(), any(), any()))
                    .thenThrow(new CustomerNotFoundException(VALID_CUSTOMER_ID));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Cliente não encontrado"));
        }

        @Test
        void shouldReturn404WhenVehicleNotFound() throws Exception {
            when(serviceOrderService.create(any(), any(), any(), any()))
                    .thenThrow(new VehicleNotFoundException(VALID_VEHICLE_ID));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Veículo não encontrado"));
        }

        @Test
        void shouldReturn422WhenVehicleNotOwnedByCustomer() throws Exception {
            when(serviceOrderService.create(any(), any(), any(), any()))
                    .thenThrow(new VehicleNotOwnedByCustomerException(VALID_VEHICLE_ID, VALID_CUSTOMER_ID));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.title").value("Veículo não pertence ao cliente"));
        }
    }

    @Nested
    class ListReceived {

        @Test
        void shouldReturn200WithPageOfReceivedServiceOrders() throws Exception {
            var serviceOrder = mockServiceOrder(ServiceOrderStatus.RECEIVED);
            Page<ServiceOrder> page = new PageImpl<>(List.of(serviceOrder), PageRequest.of(0, 10), 1);
            when(serviceOrderService.listReceived(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL + "/received"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].status").value("RECEIVED"))
                    .andExpect(jsonPath("$.content[0].vehicleId").value(VALID_VEHICLE_ID.toString()))
                    .andExpect(jsonPath("$.content[0].customerId").value(VALID_CUSTOMER_ID.toString()))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        void shouldReturn200WithEmptyPageWhenNoReceivedOrders() throws Exception {
            Page<ServiceOrder> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(serviceOrderService.listReceived(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL + "/received"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        void shouldForwardPageableParametersToService() throws Exception {
            Page<ServiceOrder> page = new PageImpl<>(List.of(), PageRequest.of(2, 5), 0);
            when(serviceOrderService.listReceived(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL + "/received")
                            .param("page", "2")
                            .param("size", "5"))
                    .andExpect(status().isOk());

            var captor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
            verify(serviceOrderService).listReceived(captor.capture());
            var captured = captor.getValue();
            org.assertj.core.api.Assertions.assertThat(captured.getPageNumber()).isEqualTo(2);
            org.assertj.core.api.Assertions.assertThat(captured.getPageSize()).isEqualTo(5);
        }
    }

    @Nested
    class ListFinished {

        @Test
        void shouldReturn200WithPageOfFinishedServiceOrders() throws Exception {
            var serviceOrder = mockServiceOrder(ServiceOrderStatus.FINISHED);
            Page<ServiceOrder> page = new PageImpl<>(List.of(serviceOrder), PageRequest.of(0, 10), 1);
            when(serviceOrderService.listFinished(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL + "/finished"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].status").value("FINISHED"))
                    .andExpect(jsonPath("$.content[0].vehicleId").value(VALID_VEHICLE_ID.toString()))
                    .andExpect(jsonPath("$.content[0].customerId").value(VALID_CUSTOMER_ID.toString()))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        void shouldReturn200WithEmptyPageWhenNoFinishedOrders() throws Exception {
            Page<ServiceOrder> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(serviceOrderService.listFinished(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL + "/finished"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        void shouldForwardPageableParametersToService() throws Exception {
            Page<ServiceOrder> page = new PageImpl<>(List.of(), PageRequest.of(2, 5), 0);
            when(serviceOrderService.listFinished(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL + "/finished")
                            .param("page", "2")
                            .param("size", "5"))
                    .andExpect(status().isOk());

            var captor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
            verify(serviceOrderService).listFinished(captor.capture());
            var captured = captor.getValue();
            org.assertj.core.api.Assertions.assertThat(captured.getPageNumber()).isEqualTo(2);
            org.assertj.core.api.Assertions.assertThat(captured.getPageSize()).isEqualTo(5);
        }
    }

    @Nested
    class GetStatusById {

        private static final UUID ORDER_ID = UUID.randomUUID();

        @Test
        void shouldReturn200WithServiceOrderStatus() throws Exception {
            when(serviceOrderService.getStatus(ORDER_ID)).thenReturn(mockServiceOrderWithId(ORDER_ID, ServiceOrderStatus.RECEIVED));

            mockMvc.perform(get(BASE_URL + "/{id}/status", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
                    .andExpect(jsonPath("$.vehicleId").value(VALID_VEHICLE_ID.toString()))
                    .andExpect(jsonPath("$.customerId").value(VALID_CUSTOMER_ID.toString()))
                    .andExpect(jsonPath("$.status").value("RECEIVED"));
        }

        @Test
        void shouldReturn404WhenOrderDoesNotExist() throws Exception {
            when(serviceOrderService.getStatus(ORDER_ID))
                    .thenThrow(new ServiceOrderNotFoundException(ORDER_ID));

            mockMvc.perform(get(BASE_URL + "/{id}/status", ORDER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Ordem de serviço não encontrada"));
        }
    }

    @Nested
    class StartDiagnosticServiceOrder {

        private static final UUID ORDER_ID = UUID.randomUUID();

        @Test
        void shouldReturn200WithUpdatedServiceOrder() throws Exception {
            when(serviceOrderService.startDiagnostic(ORDER_ID)).thenReturn(mockServiceOrder(ServiceOrderStatus.IN_DIAGNOSTIC));

            mockMvc.perform(patch(BASE_URL + "/{id}/start-diagnostic", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_DIAGNOSTIC"));
        }

        @Test
        void shouldReturn404WhenOrderDoesNotExist() throws Exception {
            when(serviceOrderService.startDiagnostic(ORDER_ID))
                    .thenThrow(new ServiceOrderNotFoundException(ORDER_ID));

            mockMvc.perform(patch(BASE_URL + "/{id}/start-diagnostic", ORDER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Ordem de serviço não encontrada"));
        }

        @Test
        void shouldReturn409WhenTransitionIsInvalid() throws Exception {
            when(serviceOrderService.startDiagnostic(ORDER_ID))
                    .thenThrow(new InvalidServiceOrderTransitionException(ServiceOrderStatus.IN_DIAGNOSTIC, ServiceOrderStatus.IN_DIAGNOSTIC));

            mockMvc.perform(patch(BASE_URL + "/{id}/start-diagnostic", ORDER_ID))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Transição de status inválida"));
        }
    }

    @Nested
    class DeliverToCustomerServiceOrder {

        private static final UUID ORDER_ID = UUID.randomUUID();

        @Test
        void shouldReturn200WithDeliveredServiceOrder() throws Exception {
            when(serviceOrderService.deliverToCustomer(ORDER_ID)).thenReturn(mockServiceOrder(ServiceOrderStatus.DELIVERED));

            mockMvc.perform(patch(BASE_URL + "/{id}/deliver", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DELIVERED"));
        }

        @Test
        void shouldReturn404WhenOrderDoesNotExist() throws Exception {
            when(serviceOrderService.deliverToCustomer(ORDER_ID))
                    .thenThrow(new ServiceOrderNotFoundException(ORDER_ID));

            mockMvc.perform(patch(BASE_URL + "/{id}/deliver", ORDER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Ordem de serviço não encontrada"));
        }

        @Test
        void shouldReturn409WhenTransitionIsInvalid() throws Exception {
            when(serviceOrderService.deliverToCustomer(ORDER_ID))
                    .thenThrow(new InvalidServiceOrderTransitionException(ServiceOrderStatus.IN_PROGRESS, ServiceOrderStatus.DELIVERED));

            mockMvc.perform(patch(BASE_URL + "/{id}/deliver", ORDER_ID))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Transição de status inválida"));
        }
    }

    @Nested
    class CancelServiceOrder {

        private static final UUID ORDER_ID = UUID.randomUUID();

        @Test
        void shouldReturn200WithCancelledServiceOrder() throws Exception {
            when(serviceOrderService.close(ORDER_ID)).thenReturn(mockServiceOrderWithId(ORDER_ID, ServiceOrderStatus.CANCELLED));

            mockMvc.perform(patch(BASE_URL + "/{id}/cancel", ORDER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"))
                    .andExpect(jsonPath("$.id").value(ORDER_ID.toString()))
                    .andExpect(jsonPath("$.vehicleId").value(VALID_VEHICLE_ID.toString()))
                    .andExpect(jsonPath("$.customerId").value(VALID_CUSTOMER_ID.toString()));
        }

        @Test
        void shouldReturn404WhenOrderDoesNotExist() throws Exception {
            when(serviceOrderService.close(ORDER_ID))
                    .thenThrow(new ServiceOrderNotFoundException(ORDER_ID));

            mockMvc.perform(patch(BASE_URL + "/{id}/cancel", ORDER_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Ordem de serviço não encontrada"));
        }

        @Test
        void shouldReturn409WhenTransitionIsInvalid() throws Exception {
            when(serviceOrderService.close(ORDER_ID))
                    .thenThrow(new InvalidServiceOrderTransitionException(ServiceOrderStatus.CANCELLED, ServiceOrderStatus.CANCELLED));

            mockMvc.perform(patch(BASE_URL + "/{id}/cancel", ORDER_ID))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Transição de status inválida"));
        }

        @Test
        void shouldReturn400WhenIdIsNotAValidUuid() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/cancel", "not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class ApproveQuote {

        private static final UUID QUOTE_ID = UUID.randomUUID();
        private static final UUID ORDER_ID = UUID.randomUUID();

        @Test
        void shouldReturn200WhenQuoteIsApproved() throws Exception {
            when(serviceOrderService.approveQuote(QUOTE_ID)).thenReturn(mockApprovedResponse());

            mockMvc.perform(patch(BASE_URL + "/quote/{id}/approve", QUOTE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(QUOTE_ID.toString()))
                    .andExpect(jsonPath("$.serviceOrderId").value(ORDER_ID.toString()))
                    .andExpect(jsonPath("$.status").value("APPROVED"))
                    .andExpect(jsonPath("$.totalPrice").value(1500.00))
                    .andExpect(jsonPath("$.quoteRefusalReason").doesNotExist());
        }

        @Test
        void shouldReturn404WhenQuoteDoesNotExist() throws Exception {
            when(serviceOrderService.approveQuote(QUOTE_ID)).thenThrow(new QuoteNotFoundException(QUOTE_ID));

            mockMvc.perform(patch(BASE_URL + "/quote/{id}/approve", QUOTE_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Orçamento não encontrado"));
        }

        @Test
        void shouldReturn409WhenTransitionIsInvalid() throws Exception {
            when(serviceOrderService.approveQuote(QUOTE_ID))
                    .thenThrow(new InvalidServiceOrderTransitionException(
                            ServiceOrderStatus.AWAITING_APPROVAL,
                            ServiceOrderStatus.AWAITING_EXECUTION));

            mockMvc.perform(patch(BASE_URL + "/quote/{id}/approve", QUOTE_ID))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Transição de status inválida"));
        }

        @Test
        void shouldReturn400WhenIdIsInvalidUuid() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/quote/{id}/approve", "invalid-uuid"))
                    .andExpect(status().isBadRequest());
        }

        private QuoteResponseDTO mockApprovedResponse() {
            return new QuoteResponseDTO(QUOTE_ID, ORDER_ID, QuoteStatus.APPROVED,
                    new BigDecimal("1500.00"), null, List.of(), List.of(), NOW, NOW);
        }
    }

    @Nested
    class ReproveQuote {

        private static final UUID QUOTE_ID = UUID.randomUUID();
        private static final UUID ORDER_ID = UUID.randomUUID();

        @Test
        void shouldReturn200WhenQuoteIsReproved() throws Exception {
            when(serviceOrderService.reproveQuote(eq(QUOTE_ID), any())).thenReturn(mockReprovedResponse());

            mockMvc.perform(patch(BASE_URL + "/quote/{id}/reprove", QUOTE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "reason": "Preço muito alto"
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(QUOTE_ID.toString()))
                    .andExpect(jsonPath("$.serviceOrderId").value(ORDER_ID.toString()))
                    .andExpect(jsonPath("$.status").value("REPROVED"))
                    .andExpect(jsonPath("$.totalPrice").value(1500.00))
                    .andExpect(jsonPath("$.quoteRefusalReason").value("Preço muito alto"));
        }

        @Test
        void shouldReturn404WhenQuoteDoesNotExist() throws Exception {
            when(serviceOrderService.reproveQuote(eq(QUOTE_ID), any())).thenThrow(new QuoteNotFoundException(QUOTE_ID));

            mockMvc.perform(patch(BASE_URL + "/quote/{id}/reprove", QUOTE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "reason": "Motivo"
                                }
                                """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Orçamento não encontrado"));
        }

        @Test
        void shouldReturn409WhenTransitionIsInvalid() throws Exception {
            when(serviceOrderService.reproveQuote(eq(QUOTE_ID), any()))
                    .thenThrow(new InvalidServiceOrderTransitionException(
                            ServiceOrderStatus.AWAITING_APPROVAL, ServiceOrderStatus.CANCELLED));

            mockMvc.perform(patch(BASE_URL + "/quote/{id}/reprove", QUOTE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "reason": "Motivo"
                                }
                                """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Transição de status inválida"));
        }

        @Test
        void shouldReturn400WhenIdIsInvalidUuid() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/quote/{id}/reprove", "invalid-uuid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "reason": "Motivo"
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        private QuoteResponseDTO mockReprovedResponse() {
            return new QuoteResponseDTO(QUOTE_ID, ORDER_ID, QuoteStatus.REPROVED,
                    new BigDecimal("1500.00"), "Preço muito alto", List.of(), List.of(), NOW, NOW);
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

    private ServiceOrder mockServiceOrder(ServiceOrderStatus status) {
        return ServiceOrder.reconstruct(UUID.randomUUID(), VALID_VEHICLE_ID, VALID_CUSTOMER_ID,
                "Barulho na suspensão", status, UUID.randomUUID(), NOW, null, NOW);
    }

    private ServiceOrder mockServiceOrderWithId(UUID id, ServiceOrderStatus status) {
        return ServiceOrder.reconstruct(id, VALID_VEHICLE_ID, VALID_CUSTOMER_ID,
                "Barulho na suspensão", status, UUID.randomUUID(), NOW, null, NOW);
    }
}