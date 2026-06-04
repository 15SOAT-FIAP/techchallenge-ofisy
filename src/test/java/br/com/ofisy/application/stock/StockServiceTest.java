package br.com.ofisy.application.stock;

import br.com.ofisy.application.stock.dto.CreateStockRequestDTO;
import br.com.ofisy.application.stock.dto.StockResponseDTO;
import br.com.ofisy.application.stock.dto.UpdateStockRequestDTO;
import br.com.ofisy.application.stock.exceptions.InsufficientStockException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.application.stockmovement.StockMovementService;
import br.com.ofisy.application.notification.usecases.CreateLowStockNotificationUseCase;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMovementService stockMovementService;

    @InjectMocks
    private StockService stockService;

    @Mock
    private CreateLowStockNotificationUseCase createLowStockNotificationUseCase;

    private Stock stock;

    private Stock lowStock;

    @BeforeEach
    void setUp() {
        stock = createStock();
        lowStock = createLowStock();
    }

    @Test
    @DisplayName("Deve criar estoque com sucesso")
    void shouldCreateStockSuccessfully() {
        when(stockRepository.save(any(Stock.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateStockRequestDTO requestDTO = new CreateStockRequestDTO(
                "Radiador",
                "Radiador de água com reservatório",
                10,
                new BigDecimal("150.00"),
                "Arrefecimento",
                2
        );

       StockResponseDTO responseDTO = stockService.create(requestDTO);

       assertThat(responseDTO.productName()).isEqualTo(requestDTO.productName());
       assertThat(responseDTO.description()).isEqualTo(requestDTO.description());
       assertThat(responseDTO.quantity()).isEqualTo(requestDTO.quantity());
       assertThat(responseDTO.unitPrice()).isEqualTo(requestDTO.unitPrice());
       assertThat(responseDTO.category()).isEqualTo(requestDTO.category());
       assertThat(responseDTO.minThreshold()).isEqualTo(requestDTO.minThreshold());
       assertThat(responseDTO.createdAt()).isNotNull();
       assertThat(responseDTO.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve atualizar estoque com sucesso")
    void shouldUpdateStockSuccessfully() {
        when(stockRepository.findById(stock.getId()))
                .thenReturn(Optional.of(stock));

        UpdateStockRequestDTO requestDTO = new UpdateStockRequestDTO(
                "Radiador - usado",
                "Radiador de água com reservatório - usado",
                new BigDecimal("190.00"),
                "Arrefecimento",
                2
        );

        StockResponseDTO responseDTO = stockService.update(stock.getId(), requestDTO);

        assertThat(responseDTO.productName()).isEqualTo(requestDTO.productName());
        assertThat(responseDTO.description()).isEqualTo(requestDTO.description());
        assertThat(responseDTO.unitPrice()).isEqualTo(requestDTO.unitPrice());
        assertThat(responseDTO.category()).isEqualTo(requestDTO.category());
        assertThat(responseDTO.minThreshold()).isEqualTo(requestDTO.minThreshold());
        assertThat(responseDTO.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve adicionar itens no estoque com sucesso")
    void shouldAddStockSuccessfully() {
        when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);

        StockResponseDTO resultDTO = stockService.addStock(stock.getId(), 10);

        assertEquals(50, resultDTO.quantity());
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    @DisplayName("Deve consumir itens no estoque com sucesso")
    void shouldConsumeStockSuccessfully() {
        when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);

        StockResponseDTO resultDTO = stockService.consumeStock(stock.getId(), 10);

        assertEquals(30, resultDTO.quantity());
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    @DisplayName("Deve lançar exception quando estoque não é localizado")
    void shouldThrowExceptionWhenStockNotFound() {
        when(stockRepository.findById(stock.getId())).thenReturn(Optional.empty());

        var stockId = stock.getId();
        assertThrows(StockNotFoundException.class, () -> stockService.addStock(stockId, 10));
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    @DisplayName("Deve lançar exception quando estoque não é insuficiente")
    void shouldThrowExceptionWhenInsufficientStock() {
        when(stockRepository.findById(lowStock.getId())).thenReturn(Optional.of(lowStock));

        var stockId = lowStock.getId();
        assertThrows(
                InsufficientStockException.class,
                () -> stockService.consumeStock(stockId, 100)
        );

        verify(stockRepository, never()).save(any(Stock.class));
    }

    private Stock createStock() {
        return Stock.create(
                "Filtro de Óleo",
                "Filtro de óleo para motor 1.6",
                40,
                new BigDecimal("100.00"),
                "Filtros",
                10
        );
    }

    private Stock createLowStock() {
        return Stock.create(
                "Filtro de combustível",
                "Filtro de combustível",
                50,
                new BigDecimal("90.00"),
                "Filtros",
                10
        );
    }
}
