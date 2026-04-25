package br.com.ofisy.interfaces.api.service;

import br.com.ofisy.application.service.ServiceAppService;
import br.com.ofisy.application.service.dto.ServiceRequestDTO;
import br.com.ofisy.application.serviceOrderService.ServiceOrderServiceApplication;
import br.com.ofisy.domain.service.Service;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ServiceControllerTest {

    @Mock
    private ServiceAppService serviceAppService;

    @Mock
    private ServiceOrderServiceApplication serviceOrderServiceApplication;

    @InjectMocks
    private ServiceController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Service service;
    private UUID serviceId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        serviceId = UUID.randomUUID();
        service = Service.create("Oil Change", "Change engine oil", new BigDecimal("50.00"));
    }

    @Test
    void getAll_shouldReturnPageOfServices() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Service> page = new PageImpl<>(List.of(service), pageable, 1);
        when(serviceAppService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/services")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getById_shouldReturnService() throws Exception {
        when(serviceAppService.findById(serviceId)).thenReturn(service);

        mockMvc.perform(get("/api/v1/services/{id}", serviceId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getByName_shouldReturnService() throws Exception {
        String name = "Oil Change";
        when(serviceAppService.findByName(name)).thenReturn(service);

        mockMvc.perform(get("/api/v1/services")
                        .param("name", name))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getExecutionTimeAverage_shouldReturnDouble() throws Exception {
        double average = 45.5;
        when(serviceOrderServiceApplication.getAverageExecutionTimeByService(serviceId)).thenReturn(average);

        mockMvc.perform(get("/api/v1/services/execution_time_average/{id}", serviceId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(String.valueOf(average)));
    }

    @Test
    void create_shouldReturnCreatedService() throws Exception {
        ServiceRequestDTO dto = new ServiceRequestDTO(
                new BigDecimal("50.00"),
                "Oil Change",
                "Change engine oil"
        );
        when(serviceAppService.create(any(ServiceRequestDTO.class))).thenReturn(service);

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
