package br.com.ofisy.application.stock.update;

import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private UpdateStockService updateStockService;

    @Test
    @DisplayName("Deve atualizar estoque com sucesso")
    void shouldUpdateStockSuccessfully() {
        Stock stock = Stock.create("Radiador", "Radiador original", 10, new BigDecimal("150.00"), "Arrefecimento", 2);
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(stock, "id", id);

        when(stockRepository.findById(id)).thenReturn(Optional.of(stock));
        when(stockRepository.save(stock)).thenReturn(stock);

        UpdateStockUseCase.UpdateStockCommand cmd = new UpdateStockUseCase.UpdateStockCommand(
                id,
                "Radiador - usado",
                "Radiador de água com reservatório - usado",
                new BigDecimal("190.00"),
                "Arrefecimento",
                2
        );

        Stock result = updateStockService.execute(cmd);

        assertThat(result.getProductName()).isEqualTo(cmd.productName());
        assertThat(result.getDescription()).isEqualTo(cmd.description());
        assertThat(result.getUnitPrice()).isEqualTo(cmd.unitPrice());
    }

    @Test
    @DisplayName("Deve lançar exception quando estoque não é localizado")
    void shouldThrowExceptionWhenStockNotFound() {
        UUID id = UUID.randomUUID();
        when(stockRepository.findById(id)).thenReturn(Optional.empty());

        UpdateStockUseCase.UpdateStockCommand cmd = new UpdateStockUseCase.UpdateStockCommand(
                id, null, null, null, null, null
        );

        assertThrows(StockNotFoundException.class, () -> updateStockService.execute(cmd));
    }
}
