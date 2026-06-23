package br.com.ofisy.adapters.controllers.serviceorderexecution;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.application.serviceorderexecution.cancel.CancelServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.complete.CompleteServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.create.CreateServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.identifybyid.IdentifyByIdServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.list.ListServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.listbyservicecatalogid.ListByServiceCatalogIdServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.listbyserviceorderid.ListByServiceOrderIdServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.listbystatus.ListByStatusServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorderexecution.start.StartExecutionUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class ServiceOrderExecutionControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ListServiceOrderExecutionUseCase listUseCase;

    @Mock
    private IdentifyByIdServiceOrderExecutionUseCase identifyUseCase;

    @Mock
    private CreateServiceOrderExecutionUseCase createUseCase;

    @Mock
    private CompleteServiceOrderExecutionUseCase completeUseCase;

    @Mock
    private CancelServiceOrderExecutionUseCase cancelUseCase;

    @Mock
    private StartExecutionUseCase startUseCase;

    @Mock
    private ListByServiceCatalogIdServiceOrderExecutionUseCase listByServiceCatalogIdUseCase;

    @Mock
    private ListByStatusServiceOrderExecutionUseCase listByStatusUseCase;

    @Mock
    private ListByServiceOrderIdServiceOrderExecutionUseCase listByServiceOrderIdUseCase;



    @Test
    void shouldGetAllServiceOrderExecutions() throws Exception {

        when(listUseCase.execute(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));


        mockMvc.perform(get("/api/v1/service_order_executions"))
                .andExpect(status().isOk());


        verify(listUseCase)
                .execute(any(Pageable.class));
    }



    @Test
    void shouldGetById() throws Exception {

        UUID id = UUID.randomUUID();

        ServiceOrderExecution execution =
                mock(ServiceOrderExecution.class);


        when(identifyUseCase.execute(id))
                .thenReturn(execution);


        mockMvc.perform(get(
                        "/api/v1/service_order_executions/{id}",
                        id
                ))
                .andExpect(status().isOk());


        verify(identifyUseCase)
                .execute(id);
    }



    @Test
    void shouldGetByServiceCatalogId() throws Exception {

        UUID serviceCatalogId = UUID.randomUUID();


        when(listByServiceCatalogIdUseCase.execute(
                eq(serviceCatalogId),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));


        mockMvc.perform(get(
                        "/api/v1/service_order_executions")
                        .param(
                                "serviceCatalogId",
                                serviceCatalogId.toString()
                        ))
                .andExpect(status().isOk());


        verify(listByServiceCatalogIdUseCase)
                .execute(eq(serviceCatalogId), any(Pageable.class));
    }



    @Test
    void shouldGetByStatus() throws Exception {


        when(listByStatusUseCase.execute(
                eq("STARTED"),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));


        mockMvc.perform(get(
                        "/api/v1/service_order_executions")
                        .param(
                                "status",
                                "STARTED"
                        ))
                .andExpect(status().isOk());


        verify(listByStatusUseCase)
                .execute(eq("STARTED"), any(Pageable.class));

    }



    @Test
    void shouldGetByServiceOrderId() throws Exception {

        UUID id = UUID.randomUUID();


        when(listByServiceOrderIdUseCase.execute(
                eq(id),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));


        mockMvc.perform(get(
                        "/api/v1/service_order_executions/service_order/{id}",
                        id
                ))
                .andExpect(status().isOk());


        verify(listByServiceOrderIdUseCase)
                .execute(eq(id), any(Pageable.class));

    }



    @Test
    void shouldCreateServiceOrderExecution() throws Exception {


        UUID catalogId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();


        ServiceOrderExecution execution =
                mock(ServiceOrderExecution.class);


        when(createUseCase.execute(any()))
                .thenReturn(execution);



        mockMvc.perform(post(
                        "/api/v1/service_order_executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "serviceCatalogId":"%s",
                  "serviceOrderId":"%s"
                }
                """.formatted(
                                catalogId,
                                orderId
                        )))
                .andExpect(status().isCreated());


        verify(createUseCase)
                .execute(any(
                        CreateServiceOrderExecutionUseCase.CreateServiceOrderExecutionCommand.class
                ));
    }



    @Test
    void shouldCompleteExecution() throws Exception {

        UUID id = UUID.randomUUID();

        when(completeUseCase.execute(id))
                .thenReturn(mock(ServiceOrderExecution.class));


        mockMvc.perform(
                        patch("/api/v1/service_order_executions/{id}/complete", id)
                )
                .andExpect(status().isOk());


        verify(completeUseCase)
                .execute(id);
    }



    @Test
    void shouldCancelExecution() throws Exception {

        UUID id = UUID.randomUUID();


        when(cancelUseCase.execute(id))
                .thenReturn(mock(ServiceOrderExecution.class));


        mockMvc.perform(
                        patch("/api/v1/service_order_executions/{id}/cancel", id)
                )
                .andExpect(status().isOk());


        verify(cancelUseCase)
                .execute(id);

    }



    @Test
    void shouldStartExecution() throws Exception {

        UUID id = UUID.randomUUID();


        when(startUseCase.execute(id))
                .thenReturn(mock(ServiceOrderExecution.class));


        mockMvc.perform(
                        patch("/api/v1/service_order_executions/{id}/start", id)
                )
                .andExpect(status().isOk());


        verify(startUseCase)
                .execute(id);

    }

}