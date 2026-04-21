package br.com.ofisy.interfaces.api;

import br.com.ofisy.application.customer.exceptions.CustomerAlreadyExistsException;
import br.com.ofisy.application.customer.exceptions.CustomerCpfCnpjNotFoundException;
import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.vehicle.exceptions.VehicleAlreadyExistsException;
import br.com.ofisy.application.vehicle.exceptions.VehicleLicensePlateNotFoundException;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.domain.customer.exceptions.InvalidCpfCnpjException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({CustomerNotFoundException.class, CustomerCpfCnpjNotFoundException.class})
    public ProblemDetail handleCustomerNotFound(RuntimeException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Cliente não encontrado");
        return problem;
    }

    @ExceptionHandler(InvalidCpfCnpjException.class)
    public ProblemDetail handleInvalidCpfCnpj(InvalidCpfCnpjException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("CPF/CNPJ inválido");
        return problem;
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ProblemDetail handleCustomerAlreadyExists(CustomerAlreadyExistsException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Cliente já existe");
        return problem;
    }

    @ExceptionHandler(VehicleNotFoundException.class)
    public ProblemDetail handleVehicleNotFound(VehicleNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Veículo não encontrado");
        return problem;
    }

    @ExceptionHandler(VehicleLicensePlateNotFoundException.class)
    public ProblemDetail handleVehicleLicensePlateNotFound(VehicleLicensePlateNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Veículo não encontrado pela placa");
        return problem;
    }

    @ExceptionHandler(VehicleAlreadyExistsException.class)
    public ProblemDetail handleVehicleAlreadyExists(VehicleAlreadyExistsException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Veículo já existe");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Requisição inválida");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Erro de validação");
        problem.setDetail("Um ou mais campos são inválidos");
        problem.setProperty("errors", fieldErrors);
        return problem;
    }
}