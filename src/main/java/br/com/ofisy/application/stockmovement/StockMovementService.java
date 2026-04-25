package br.com.ofisy.application.stockmovement;

import br.com.ofisy.application.stockmovement.dto.StockMovementRequestDTO;
import br.com.ofisy.application.stockmovement.dto.StockMovementResponseDTO;
import br.com.ofisy.domain.stockmovement.StockMovement;
import br.com.ofisy.domain.stockmovement.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public StockMovementResponseDTO registerMovement(StockMovementRequestDTO stockMovementRequestDTO) {
        StockMovement stockMovement = StockMovementMapper.toDomain(stockMovementRequestDTO);

        StockMovement savedStockMovement = stockMovementRepository.save(stockMovement);

        return StockMovementMapper.toDTO(savedStockMovement);
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponseDTO> findAll(Pageable pageable) {
        Page<StockMovement> stockMovementList = stockMovementRepository.findAll(pageable);

        return stockMovementList.map(StockMovementMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponseDTO> findByStockId(UUID stockId, Pageable pageable) {
        Page<StockMovement> stockMovementList = stockMovementRepository.findByStockId(stockId, pageable);

        return stockMovementList.map(StockMovementMapper::toDTO);
    }
}
