package br.com.ofisy.interfaces.api.serviceOrderExecution;

import br.com.ofisy.application.serviceOrderExecution.ServiceOrderExecutionService;
import br.com.ofisy.application.serviceOrderExecution.dto.ServiceOrderExecutionRequestDTO;
import br.com.ofisy.domain.serviceOrderExecution.ServiceOrderExecution;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderExecutionControllerTest {

    @Mock
    private ServiceOrderExecutionService serviceOrderExecutionService;

    @InjectMocks
    private ServiceOrderExecutionController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ServiceOrderExecution serviceOrderExecution;
    private UUID serviceOrderExecutionId;
    private UUID serviceCatalogId;
    private UUID serviceOrderId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        serviceOrderExecutionId = UUID.randomUUID();
        serviceCatalogId = UUID.randomUUID();
        serviceOrderId = UUID.randomUUID();
        serviceOrderExecution = ServiceOrderExecution.create(serviceCatalogId, serviceOrderId);
    }

    @Test
    void getAll_shouldReturnPageOfServiceOrderExecutions() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceOrderExecution> page = new PageImpl<>(List.of(serviceOrderExecution), pageable, 1);
        when(serviceOrderExecutionService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/service_order_executions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getById_shouldReturnServiceOrderExecution() throws Exception {
        when(serviceOrderExecutionService.findById(serviceOrderExecutionId)).thenReturn(serviceOrderExecution);

        mockMvc.perform(get("/api/v1/service_order_executions/{id}", serviceOrderExecutionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getByServiceCatalogId_shouldReturnPageOfServiceOrderExecutions() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceOrderExecution> page = new PageImpl<>(List.of(serviceOrderExecution), pageable, 1);
        when(serviceOrderExecutionService.findByServiceCatalogId(any(UUID.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/service_order_executions")
                        .param("serviceCatalogId", serviceCatalogId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getByStatus_shouldReturnPageOfServiceOrderExecutions() throws Exception {
        String status = "PENDING";
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceOrderExecution> page = new PageImpl<>(List.of(serviceOrderExecution), pageable, 1);
        when(serviceOrderExecutionService.findByStatus(status, pageable)).thenReturn(page);

        mockMvc.perform(get("/api/v1/service_order_executions")
                        .param("status", status)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getByServiceOrderId_shouldReturnPageOfServiceOrderExecutions() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceOrderExecution> page = new PageImpl<>(List.of(serviceOrderExecution), pageable, 1);
        when(serviceOrderExecutionService.findByServiceOrderId(any(UUID.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/service_order_executions/service_order/{id}", serviceOrderId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void create_shouldReturnCreatedServiceOrderExecution() throws Exception {
        ServiceOrderExecutionRequestDTO dto = new ServiceOrderExecutionRequestDTO(serviceCatalogId, serviceOrderId);
        when(serviceOrderExecutionService.create(any(ServiceOrderExecutionRequestDTO.class))).thenReturn(serviceOrderExecution);

        mockMvc.perform(post("/api/v1/service_order_executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void complete_shouldReturnCompletedServiceOrderExecution() throws Exception {
        when(serviceOrderExecutionService.complete(serviceOrderExecutionId)).thenReturn(serviceOrderExecution);

        mockMvc.perform(patch("/api/v1/service_order_executions/{id}/complete", serviceOrderExecutionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void cancel_shouldReturnCancelledServiceOrderExecution() throws Exception {
        when(serviceOrderExecutionService.cancel(serviceOrderExecutionId)).thenReturn(serviceOrderExecution);

        mockMvc.perform(patch("/api/v1/service_order_executions/{id}/cancel", serviceOrderExecutionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void start_shouldReturnStartedServiceOrderExecution() throws Exception {
        when(serviceOrderExecutionService.start(serviceOrderExecutionId)).thenReturn(serviceOrderExecution);

        mockMvc.perform(patch("/api/v1/service_order_executions/{id}/start", serviceOrderExecutionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
