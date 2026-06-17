package br.com.ofisy.adapters.controllers.stockmovement;

import br.com.ofisy.application.stockmovement.list.ListStockMovementUseCase;
import br.com.ofisy.application.stockmovement.listbystock.ListByStockStockMovementUseCase;
import br.com.ofisy.domain.stockmovement.MovementType;
import br.com.ofisy.domain.stockmovement.StockMovement;
import br.com.ofisy.interfaces.api.ControllerTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockMovementController.class)
@WithMockUser
class StockMovementControllerTest extends ControllerTestBase {

    private static final String BASE_URL = "/api/v1/stock-movements";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListStockMovementUseCase listStockMovementUseCase;

    @MockitoBean
    private ListByStockStockMovementUseCase listByStockStockMovementUseCase;

    @Nested
    class GetAllStockMovements {

        @Test
        @DisplayName("Deve retornar página com estoques e retornar 200")
        void shouldReturn200WithPageOfStockMovements() throws Exception {
            var movement = mockMovement(UUID.randomUUID());
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(movement), pageable, 1);

            when(listStockMovementUseCase.execute(any())).thenReturn(page);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].quantity").value(5))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Deve retornar página vazia e retornar 200")
        void shouldReturnEmptyPage() throws Exception {
            var pageable = PageRequest.of(0, 10);

            when(listStockMovementUseCase.execute(any()))
                    .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    class GetByStockId {

        @Test
        @DisplayName("Deve retornar movimentações por stockId e retornar 200")
        void shouldReturn200WhenFilteringByStockId() throws Exception {
            var stockId = UUID.randomUUID();
            var movement = mockMovement(stockId);
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(movement), pageable, 1);

            when(listByStockStockMovementUseCase.execute(eq(stockId), any())).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                            .param("stockId", stockId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].stockId").value(stockId.toString()));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não houver movimentações para o stockId")
        void shouldReturnEmptyWhenStockIdHasNoMovements() throws Exception {
            var stockId = UUID.randomUUID();
            var pageable = PageRequest.of(0, 10);

            when(listByStockStockMovementUseCase.execute(eq(stockId), any()))
                    .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

            mockMvc.perform(get(BASE_URL)
                            .param("stockId", stockId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("Deve retornar 400 quando stockId for inválido")
        void shouldReturn400WhenStockIdIsInvalid() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("stockId", "invalid-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    private StockMovement mockMovement(UUID stockId) {
        StockMovement movement = StockMovement.create(stockId, MovementType.IN, 5, 10, 15);
        ReflectionTestUtils.setField(movement, "id", UUID.randomUUID());
        return movement;
    }
}
