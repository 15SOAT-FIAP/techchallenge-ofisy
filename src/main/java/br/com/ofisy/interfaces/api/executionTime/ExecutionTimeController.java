package br.com.ofisy.interfaces.api.executionTime;

import br.com.ofisy.application.executionTime.ExecutionTimeMapper;
import br.com.ofisy.application.executionTime.ExecutionTimeService;
import br.com.ofisy.application.service.dto.ExecutionTimeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/execution-times")
@RequiredArgsConstructor
public class ExecutionTimeController {

    private final ExecutionTimeService service;
    private final ExecutionTimeMapper mapper;

    @PostMapping
    public ResponseEntity<ExecutionTimeResponseDTO> startExecution(@RequestParam UUID serviceId) {
        var executionTime = service.create(serviceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(executionTime));
    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<ExecutionTimeResponseDTO> finishExecution(@PathVariable UUID id) {
        var executionTime = service.finish(id);
        return ResponseEntity.ok(mapper.toResponse(executionTime));
    }

    @GetMapping("/average")
    public ResponseEntity<Double> getAverageExecutionTime(@RequestParam UUID catalogServiceId) {
        var average = service.getAverageExecutionTimeByService(catalogServiceId);
        return ResponseEntity.ok(average);
    }
}

