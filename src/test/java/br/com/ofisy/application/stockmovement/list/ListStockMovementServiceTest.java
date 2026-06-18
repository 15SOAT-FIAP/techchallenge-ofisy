package br.com.ofisy.application.stockmovement.list;

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
class ListStockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private ListStockMovementService listStockMovementService;

    @Test
    @DisplayName("Deve retornar página com movimentações de estoque")
    void shouldReturnPageOfMovements() {
        var pageable = PageRequest.of(0, 10);
        var movement = StockMovement.create(UUID.randomUUID(), MovementType.IN, 10, 40, 50);
        var page = new PageImpl<>(List.of(movement), pageable, 1);

        when(stockMovementRepository.findAll(pageable)).thenReturn(page);

        var result = listStockMovementService.execute(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMovementType()).isEqualTo(MovementType.IN);
        verify(stockMovementRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há movimentações")
    void shouldReturnEmptyPageWhenNoMovements() {
        var pageable = PageRequest.of(0, 10);
        var emptyPage = new PageImpl<StockMovement>(List.of(), pageable, 0);

        when(stockMovementRepository.findAll(pageable)).thenReturn(emptyPage);

        var result = listStockMovementService.execute(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(stockMovementRepository).findAll(pageable);
    }
}
