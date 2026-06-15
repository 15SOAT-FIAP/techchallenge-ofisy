package br.com.ofisy.adapters.controllers.stock;

import br.com.ofisy.application.stock.add.AddStockUseCase;
import br.com.ofisy.application.stock.consume.ConsumeStockUseCase;
import br.com.ofisy.application.stock.create.CreateStockUseCase;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.application.stock.identifybyid.IdentifyByIdStockUseCase;
import br.com.ofisy.application.stock.identifybyproduct.IdentifyByProductStockUseCase;
import br.com.ofisy.application.stock.list.ListStockUseCase;
import br.com.ofisy.application.stock.update.UpdateStockUseCase;
import br.com.ofisy.domain.stock.Stock;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
@WithMockUser
class StockControllerTest extends ControllerTestBase {

    private static final String BASE_URL = "/api/v1/stocks";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateStockUseCase createStockUseCase;

    @MockitoBean
    private UpdateStockUseCase updateStockUseCase;

    @MockitoBean
    private AddStockUseCase addStockUseCase;

    @MockitoBean
    private ConsumeStockUseCase consumeStockUseCase;

    @MockitoBean
    private ListStockUseCase listStockUseCase;

    @MockitoBean
    private IdentifyByIdStockUseCase identifyByIdStockUseCase;

    @MockitoBean
    private IdentifyByProductStockUseCase identifyByProductStockUseCase;

    @Nested
    class GetAllStocks {

        @Test
        @DisplayName("Deve retornar página com estoques e retornar 200")
        void shouldReturn200WithPageOfStocks() throws Exception {
            var stock = mockStock(UUID.randomUUID(), "Produto A");
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(stock), pageable, 1);

            when(listStockUseCase.execute(any())).thenReturn(page);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].productName").value("Produto A"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há estoque")
        void shouldReturnEmptyPage() throws Exception {
            var pageable = PageRequest.of(0, 10);

            when(listStockUseCase.execute(any()))
                    .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    class GetStockById {

        @Test
        @DisplayName("Deve encontrar estoque e retornar 200")
        void shouldReturn200WhenFound() throws Exception {
            var id = UUID.randomUUID();
            var stock = mockStock(id, "Produto A");

            when(identifyByIdStockUseCase.execute(id)).thenReturn(stock);

            mockMvc.perform(get(BASE_URL + "/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productName").value("Produto A"));
        }

        @Test
        @DisplayName("Deve retornar 404 quando estoque não é encontrado")
        void shouldReturn404WhenNotFound() throws Exception {
            var id = UUID.randomUUID();

            when(identifyByIdStockUseCase.execute(id))
                    .thenThrow(new StockNotFoundException(id));

            mockMvc.perform(get(BASE_URL + "/{id}", id))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 400 se ID for inválido")
        void shouldReturn400WhenInvalidUUID() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", "invalid-id"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetByProductName {

        @Test
        @DisplayName("Deve encontrar estoque e retornar 200")
        void shouldReturn200WhenFound() throws Exception {
            var stock = mockStock(UUID.randomUUID(), "Produto A");

            when(identifyByProductStockUseCase.execute("Produto A")).thenReturn(stock);

            mockMvc.perform(get(BASE_URL + "/productName/{productName}", "Produto A"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productName").value("Produto A"));
        }

        @Test
        @DisplayName("Deve retornar 404 quando estoque não é encontrado")
        void shouldReturn404WhenNotFound() throws Exception {
            when(identifyByProductStockUseCase.execute("Inexistente"))
                    .thenThrow(new StockNotFoundException("Inexistente"));

            mockMvc.perform(get(BASE_URL + "/productName/{productName}", "Inexistente"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class CreateStock {

        @Test
        @DisplayName("Deve criar estoque e retornar 201")
        void shouldReturn201WhenCreated() throws Exception {
            var stock = mockStock(UUID.randomUUID(), "Produto A");

            when(createStockUseCase.execute(any())).thenReturn(stock);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                        {
                          "productName": "Produto A",
                          "description": "Descrição do produto",
                          "quantity": 10,
                          "unitPrice": 99.90,
                          "category": "Categoria X",
                          "minThreshold": 5
                        }
                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.productName").value("Produto A"));
        }

        @Test
        @DisplayName("Deve retornar 400 quando body é inválido")
        void shouldReturn400WhenInvalidBody() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class AddStock {

        @Test
        @DisplayName("Deve adicionar estoque e retornar 200")
        void shouldReturn200WhenStockAdded() throws Exception {
            var id = UUID.randomUUID();
            var stock = mockStock(id, "Produto A");

            when(addStockUseCase.execute(any())).thenReturn(stock);

            mockMvc.perform(post(BASE_URL + "/{id}/add", id)
                            .param("quantity", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productName").value("Produto A"));
        }
    }

    @Nested
    class ConsumeStock {

        @Test
        @DisplayName("Deve consumir estoque e retornar 200")
        void shouldReturn200WhenStockConsumed() throws Exception {
            var id = UUID.randomUUID();
            var stock = mockStock(id, "Produto A");

            when(consumeStockUseCase.execute(any())).thenReturn(stock);

            mockMvc.perform(post(BASE_URL + "/{id}/consume", id)
                            .param("quantity", "3"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class UpdateStock {

        @Test
        @DisplayName("Deve atualizar estoque e retornar 200")
        void shouldReturn200WhenUpdated() throws Exception {
            var id = UUID.randomUUID();
            var stock = mockStock(id, "Produto Atualizado");

            when(updateStockUseCase.execute(any())).thenReturn(stock);

            mockMvc.perform(put(BASE_URL + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "productName": "Produto Atualizado",
                                  "minThreshold": 8
                                }
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productName").value("Produto Atualizado"));
        }
    }

    private Stock mockStock(UUID id, String productName) {
        Stock stock = Stock.create(productName, "Descrição do produto", 10, BigDecimal.valueOf(99.90), "Categoria X", 5);
        ReflectionTestUtils.setField(stock, "id", id);
        return stock;
    }
}
