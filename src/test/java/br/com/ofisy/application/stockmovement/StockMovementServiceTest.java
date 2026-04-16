package br.com.ofisy.application.stockmovement;

import br.com.ofisy.domain.stockmovement.MovementType;
import br.com.ofisy.domain.stockmovement.StockMovement;
import br.com.ofisy.domain.stockmovement.StockMovementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private StockMovementService stockMovementService;

    @Test
    @DisplayName("Deve registrar moviementação de entrada no estoque")
    void shouldRegisterInMovementSuccessfully() {
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);

        StockMovement stockIn = createInMovement();
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(stockIn);

        stockMovementService.registerMovement(
                UUID.randomUUID(),
                MovementType.IN,
                10,
                90,
                100
        );

        verify(stockMovementRepository).save(captor.capture());

        StockMovement saved = captor.getValue();

        assertEquals(MovementType.IN, saved.getMovementType());
        assertEquals(10, saved.getQuantity());
        assertEquals(90, saved.getPreviousQuantity());
        assertEquals(100, saved.getNewQuantity());
    }

    @Test
    @DisplayName("Deve registrar moviementação de saída no estoque")
    void shouldRegisterOutMovementSuccessfully() {
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);

        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UUID stockId = UUID.randomUUID();

        stockMovementService.registerMovement(
                stockId,
                MovementType.OUT,
                10,
                100,
                90
        );

        verify(stockMovementRepository).save(captor.capture());

        StockMovement saved = captor.getValue();

        assertEquals(stockId, saved.getStockId());
        assertEquals(MovementType.OUT, saved.getMovementType());
        assertEquals(10, saved.getQuantity());
        assertEquals(100, saved.getPreviousQuantity());
        assertEquals(90, saved.getNewQuantity());
    }

    private StockMovement createInMovement() {
        return StockMovement.create(
                UUID.randomUUID(),
                MovementType.IN,
                10,
                90,
                100
        );
    }
}
