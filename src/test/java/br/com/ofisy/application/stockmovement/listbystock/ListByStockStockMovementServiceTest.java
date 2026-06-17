package br.com.ofisy.application.stockmovement.listbystock;

import br.com.ofisy.domain.stockmovement.MovementType;
import br.com.ofisy.domain.stockmovement.StockMovement;
import br.com.ofisy.domain.stockmovement.StockMovementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListByStockStockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private ListByStockStockMovementService listByStockStockMovementService;

    @Test
    @DisplayName("Deve retornar movimentações filtradas pelo stockId")
    void shouldReturnMovementsForGivenStockId() {
        var stockId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var movement = StockMovement.create(stockId, MovementType.OUT, 5, 50, 45);
        var page = new PageImpl<>(List.of(movement), pageable, 1);

        when(stockMovementRepository.findByStockId(stockId, pageable)).thenReturn(page);

        var result = listByStockStockMovementService.execute(stockId, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStockId()).isEqualTo(stockId);
        assertThat(result.getContent().get(0).getMovementType()).isEqualTo(MovementType.OUT);
        verify(stockMovementRepository).findByStockId(stockId, pageable);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há movimentações para o stockId")
    void shouldReturnEmptyPageWhenNoMovementsForStockId() {
        var stockId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 10);
        var emptyPage = new PageImpl<StockMovement>(List.of(), pageable, 0);

        when(stockMovementRepository.findByStockId(stockId, pageable)).thenReturn(emptyPage);

        var result = listByStockStockMovementService.execute(stockId, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(stockMovementRepository).findByStockId(stockId, pageable);
    }
}
