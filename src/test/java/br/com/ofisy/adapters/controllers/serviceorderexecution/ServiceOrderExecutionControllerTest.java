package br.com.ofisy.adapters.controllers.serviceorderexecution;

import br.com.ofisy.application.serviceorderexecution.cancel.CancelServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.complete.CompleteServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.create.CreateServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.identifybyid.IdentifyByIdServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.list.ListServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.listbyservicecatalogid.ListByServiceCatalogIdServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.listbyserviceorderid.ListByServiceOrderIdServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.listbystatus.ListByStatusServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.start.StartExecutionUseCase;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.interfaces.api.ControllerTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ServiceOrderExecutionController.class)
@WithMockUser
class ServiceOrderExecutionControllerTest extends ControllerTestBase {


    private static final String BASE_URL =
            "/api/v1/service_order_executions";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListServiceOrderExecutionUseCase listUseCase;

    @MockitoBean
    private IdentifyByIdServiceOrderExecutionUseCase identifyUseCase;

    @MockitoBean
    private CreateServiceOrderExecutionUseCase createUseCase;

    @MockitoBean
    private CompleteServiceOrderExecutionUseCase completeUseCase;

    @MockitoBean
    private CancelServiceOrderExecutionUseCase cancelUseCase;

    @MockitoBean
    private StartExecutionUseCase startUseCase;

    @MockitoBean
    private ListByServiceCatalogIdServiceOrderExecutionUseCase listByServiceCatalogIdUseCase;

    @MockitoBean
    private ListByStatusServiceOrderExecutionUseCase listByStatusUseCase;

    @MockitoBean
    private ListByServiceOrderIdServiceOrderExecutionUseCase listByServiceOrderIdUseCase;

    @Nested
    class GetAll {
        @Test
        @DisplayName("Deve retornar execuções e status 200")
        void shouldReturn200WithExecutions() throws Exception {
            var execution = mockExecution();

            var page =
                    new PageImpl<>(
                            List.of(execution),
                            PageRequest.of(0,10),
                            1
                    );

            when(listUseCase.execute(any()))
                    .thenReturn(page);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    class GetById {
        @Test
        @DisplayName("Deve buscar execução por id")
        void shouldReturnExecutionById() throws Exception {
            UUID id = UUID.randomUUID();

            var execution = mockExecution();
            ReflectionTestUtils.setField(execution,"id",id);

            when(identifyUseCase.execute(id))
                    .thenReturn(execution);

            mockMvc.perform(
                            get(BASE_URL + "/{id}", id)
                    )
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve retornar 400 para UUID inválido")
        void shouldReturn400InvalidUUID() throws Exception {
            mockMvc.perform(
                            get(BASE_URL + "/invalid")
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetByFilters {
        @Test
        @DisplayName("Deve buscar por service catalog id")
        void shouldReturnByCatalogId() throws Exception {
            UUID id = UUID.randomUUID();

            when(
                    listByServiceCatalogIdUseCase.execute(any(), any())
            )
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(
                            get(BASE_URL)
                                    .param(
                                            "serviceCatalogId",
                                            id.toString()
                                    )
                    )
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve buscar por status")
        void shouldReturnByStatus() throws Exception {
            when(
                    listByStatusUseCase.execute(any(),any())
            )
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(
                            get(BASE_URL)
                                    .param("status","STARTED")
                    )
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class Create {
        @Test
        @DisplayName("Deve criar execução e retornar 201")
        void shouldCreateExecution() throws Exception {
            when(createUseCase.execute(any()))
                    .thenReturn(mockExecution());

            mockMvc.perform(
                            post(BASE_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                    {
                      "serviceCatalogId":"%s",
                      "serviceOrderId":"%s"
                    }
                    """
                                            .formatted(
                                                    UUID.randomUUID(),
                                                    UUID.randomUUID()
                                            ))
                    )
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Deve retornar 400 quando body inválido")
        void shouldReturn400InvalidBody() throws Exception {
            mockMvc.perform(
                            post(BASE_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}")
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Actions {
        @Test
        @DisplayName("Deve iniciar execução")
        void shouldStart() throws Exception {
            UUID id = UUID.randomUUID();

            when(startUseCase.execute(id))
                    .thenReturn(mockExecution());

            mockMvc.perform(
                            patch(BASE_URL + "/{id}/start", id)
                    )
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve completar execução")
        void shouldComplete() throws Exception {
            UUID id = UUID.randomUUID();

            when(completeUseCase.execute(id))
                    .thenReturn(mockExecution());

            mockMvc.perform(
                            patch(BASE_URL + "/{id}/complete", id)
                    )
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve cancelar execução")
        void shouldCancel() throws Exception {
            UUID id = UUID.randomUUID();

            when(cancelUseCase.execute(id))
                    .thenReturn(mockExecution());

            mockMvc.perform(
                            patch(BASE_URL + "/{id}/cancel", id)
                    )
                    .andExpect(status().isOk());
        }

    }

    private ServiceOrderExecution mockExecution() {
        ServiceOrderExecution execution =
                ServiceOrderExecution.create(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        ReflectionTestUtils.setField(
                execution,
                "id",
                UUID.randomUUID()
        );

        return execution;
    }
}