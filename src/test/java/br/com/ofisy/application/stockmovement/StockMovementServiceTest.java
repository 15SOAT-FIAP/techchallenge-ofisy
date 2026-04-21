package br.com.ofisy.application.stockmovement;

import br.com.ofisy.application.stockmovement.dto.StockMovementRequestDTO;
import br.com.ofisy.application.stockmovement.dto.StockMovementResponseDTO;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private StockMovementService stockMovementService;

    @Test
    @DisplayName("Deve registrar movimentação de entrada no estoque")
    void shouldRegisterInMovementSuccessfully() {
        StockMovement stockIn = createInMovement();

        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenReturn(stockIn);

        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO(
                UUID.randomUUID(),
                MovementType.IN,
                10,
                90,
                100
        );

        StockMovementResponseDTO responseDTO =
                stockMovementService.registerMovement(requestDTO);

        verify(stockMovementRepository).save(any());

        assertThat(responseDTO.movementType()).isEqualTo(MovementType.IN);
        assertThat(responseDTO.quantity()).isEqualTo(10);
        assertThat(responseDTO.previousQuantity()).isEqualTo(90);
        assertThat(responseDTO.newQuantity()).isEqualTo(100);
        assertThat(responseDTO.createdAt()).isNotNull();
        assertThat(responseDTO.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve registrar movimentação de saída no estoque")
    void shouldRegisterOutMovementSuccessfully() {
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UUID stockId = UUID.randomUUID();

        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO(
                stockId,
                MovementType.OUT,
                10,
                100,
                90
        );

        StockMovementResponseDTO responseDTO =
                stockMovementService.registerMovement(requestDTO);

        verify(stockMovementRepository).save(any());

        assertThat(responseDTO.movementType()).isEqualTo(MovementType.OUT);
        assertThat(responseDTO.quantity()).isEqualTo(10);
        assertThat(responseDTO.previousQuantity()).isEqualTo(100);
        assertThat(responseDTO.newQuantity()).isEqualTo(90);
        assertThat(responseDTO.createdAt()).isNotNull();
        assertThat(responseDTO.updatedAt()).isNotNull();
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
