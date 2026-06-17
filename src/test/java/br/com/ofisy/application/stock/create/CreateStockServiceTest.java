package br.com.ofisy.application.stock.create;

import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateStockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private CreateStockService createStockService;

    @Test
    @DisplayName("Deve criar estoque com sucesso")
    void shouldCreateStockSuccessfully() {
        when(stockRepository.save(any(Stock.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateStockUseCase.CreateStockCommand cmd = new CreateStockUseCase.CreateStockCommand(
                "Radiador",
                "Radiador de água com reservatório",
                10,
                new BigDecimal("150.00"),
                "Arrefecimento",
                2
        );

        Stock result = createStockService.execute(cmd);

        assertThat(result.getProductName()).isEqualTo(cmd.productName());
        assertThat(result.getDescription()).isEqualTo(cmd.description());
        assertThat(result.getQuantity()).isEqualTo(cmd.quantity());
        assertThat(result.getUnitPrice()).isEqualTo(cmd.unitPrice());
        assertThat(result.getCategory()).isEqualTo(cmd.category());
        assertThat(result.getMinThreshold()).isEqualTo(cmd.minThreshold());
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }
}
