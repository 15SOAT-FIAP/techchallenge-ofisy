package br.com.ofisy.interfaces.api.service;

import br.com.ofisy.application.service.ServiceAppService;
import br.com.ofisy.application.service.dto.ServiceRequestDTO;
import br.com.ofisy.application.serviceOrderService.ServiceOrderServiceApplication;
import br.com.ofisy.domain.service.Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController implements ServiceApi {
    private ServiceAppService serviceAppService;
    private ServiceOrderServiceApplication serviceOrderServiceApplication;

    @GetMapping
    public ResponseEntity<Page<Service>> getAll(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(serviceAppService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Service> getById(@PathVariable UUID id) {

        return ResponseEntity.status(HttpStatus.OK).body(serviceAppService.findById(id));

    }

    @GetMapping(params = "name")
    public ResponseEntity<Service> getByName(@RequestParam String name) {

        return ResponseEntity.ok(serviceAppService.findByName(name));

    }

    @GetMapping("execution_time_average/{id}")
    public ResponseEntity<Double> getExecutionTimeAverage(@PathVariable UUID id) {

        return ResponseEntity.ok(serviceOrderServiceApplication.getAverageExecutionTimeByService(id));
    }

    @PostMapping
    public ResponseEntity<Service> create(
            @Valid @RequestBody ServiceRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(serviceAppService.create(dto));
    }
}

