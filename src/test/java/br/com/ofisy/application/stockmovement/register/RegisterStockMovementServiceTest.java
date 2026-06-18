package br.com.ofisy.application.stockmovement.register;

import br.com.ofisy.domain.stockmovement.MovementType;
import br.com.ofisy.domain.stockmovement.StockMovement;
import br.com.ofisy.domain.stockmovement.StockMovementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterStockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private RegisterStockMovementService registerStockMovementService;

    @Test
    @DisplayName("Deve registrar movimentação de entrada com sucesso")
    void shouldRegisterInMovementSuccessfully() {
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var cmd = new RegisterStockMovementUseCase.RegisterStockMovementCommand(
                UUID.randomUUID(), MovementType.IN, 10, 90, 100
        );

        StockMovement result = registerStockMovementService.execute(cmd);

        assertThat(result.getStockId()).isEqualTo(cmd.stockId());
        assertThat(result.getMovementType()).isEqualTo(MovementType.IN);
        assertThat(result.getQuantity()).isEqualTo(10);
        assertThat(result.getPreviousQuantity()).isEqualTo(90);
        assertThat(result.getNewQuantity()).isEqualTo(100);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("Deve registrar movimentação de saída com sucesso")
    void shouldRegisterOutMovementSuccessfully() {
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var cmd = new RegisterStockMovementUseCase.RegisterStockMovementCommand(
                UUID.randomUUID(), MovementType.OUT, 10, 100, 90
        );

        StockMovement result = registerStockMovementService.execute(cmd);

        assertThat(result.getMovementType()).isEqualTo(MovementType.OUT);
        assertThat(result.getQuantity()).isEqualTo(10);
        assertThat(result.getPreviousQuantity()).isEqualTo(100);
        assertThat(result.getNewQuantity()).isEqualTo(90);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }
}
