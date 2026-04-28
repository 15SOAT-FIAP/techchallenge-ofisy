package br.com.ofisy.interfaces.api.serviceCatalog;

import br.com.ofisy.application.serviceCatalog.ServiceCatalogService;
import br.com.ofisy.application.serviceCatalog.dto.ServiceCatalogRequestDTO;
import br.com.ofisy.application.serviceOrderExecution.ServiceOrderExecutionService;
import br.com.ofisy.domain.serviceCatalog.ServiceCatalog;
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
    private ServiceCatalogService serviceCatalogService;

    @Mock
    private ServiceOrderExecutionService serviceOrderExecutionService;

    @InjectMocks
    private ServiceCatalogController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ServiceCatalog serviceCatalog;
    private UUID serviceCatalogId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        serviceCatalogId = UUID.randomUUID();
        serviceCatalog = ServiceCatalog.create("Oil Change", "Change engine oil", new BigDecimal("50.00"));
    }

    @Test
    void getAll_shouldReturnPageOfServices() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceCatalog> page = new PageImpl<>(List.of(serviceCatalog), pageable, 1);
        when(serviceCatalogService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/services-catalog")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getById_shouldReturnService() throws Exception {
        when(serviceCatalogService.findById(serviceCatalogId)).thenReturn(serviceCatalog);

        mockMvc.perform(get("/api/v1/services-catalog/{id}", serviceCatalogId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getByName_shouldReturnService() throws Exception {
        String name = "Oil Change";
        when(serviceCatalogService.findByName(name)).thenReturn(serviceCatalog);

        mockMvc.perform(get("/api/v1/services-catalog")
                        .param("name", name))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getExecutionTimeAverage_shouldReturnDouble() throws Exception {
        double average = 45.5;
        when(serviceOrderExecutionService.getAverageExecutionTimeByService(serviceCatalogId)).thenReturn(average);

        mockMvc.perform(get("/api/v1/services-catalog/execution_time_average/{id}", serviceCatalogId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(String.valueOf(average)));
    }

    @Test
    void create_shouldReturnCreatedService() throws Exception {
        ServiceCatalogRequestDTO dto = new ServiceCatalogRequestDTO(
                new BigDecimal("50.00"),
                "Oil Change",
                "Change engine oil"
        );
        when(serviceCatalogService.create(any(ServiceCatalogRequestDTO.class))).thenReturn(serviceCatalog);

        mockMvc.perform(post("/api/v1/services-catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
