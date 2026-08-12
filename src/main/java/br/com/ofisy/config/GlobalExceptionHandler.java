package br.com.ofisy.config;

import br.com.ofisy.application.customer.exceptions.CustomerAlreadyExistsException;
import br.com.ofisy.application.customer.exceptions.CustomerCpfCnpjNotFoundException;
import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.application.quote.exceptions.QuoteAlreadyExistsException;
import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.application.serviceorder.exceptions.QuoteNotFoundForServiceOrderException;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.stock.exceptions.InsufficientStockException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.application.user.exceptions.EmailNotFoundException;
import br.com.ofisy.application.user.exceptions.UserNotFoundException;
import br.com.ofisy.application.vehicle.exceptions.VehicleAlreadyExistsException;
import br.com.ofisy.application.vehicle.exceptions.VehicleLicensePlateNotFoundException;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.domain.customer.exceptions.CustomerAlreadyActiveException;
import br.com.ofisy.domain.customer.exceptions.CustomerAlreadyInactiveException;
import br.com.ofisy.domain.customer.exceptions.InvalidCpfCnpjException;
import br.com.ofisy.domain.notification.exceptions.InvalidNotificationMessageException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteDataException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteItemException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
import br.com.ofisy.domain.user.exceptions.EmailAlreadyExistsException;
import br.com.ofisy.domain.user.exceptions.InactiveUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @ExceptionHandler({CustomerAlreadyActiveException.class, CustomerAlreadyInactiveException.class})
    public ProblemDetail handleCustomerActivationConflict(RuntimeException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflito no status do cliente");
        return problem;
    }

    @ExceptionHandler(InvalidCpfCnpjException.class)
    public ProblemDetail handleInvalidCpfCnpj(InvalidCpfCnpjException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("CPF/CNPJ inválido");
        return problem;
    }

    @ExceptionHandler(InvalidNotificationMessageException.class)
    public ProblemDetail handleInvalidNotificationMessage(InvalidNotificationMessageException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Mensagem de notificação inválida");
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

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Usuário não encontrado");
        return problem;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Email já cadastrado");
        return problem;
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ProblemDetail handleEmailAddressNotFound(EmailNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Email informado não encontrado");
        return problem;
    }

    @ExceptionHandler(InactiveUserException.class)
    public ProblemDetail handleDisabled(InactiveUserException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Usuário inativo");
        return problem;
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos");
        problem.setTitle("Não autorizado");
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Erro de validação");
        problem.setDetail("Um ou mais campos são inválidos ou contêm valores não permitidos");
        return problem;
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ProblemDetail handleUsernameNotFound(UsernameNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Usuário não autorizado");
        return problem;
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ProblemDetail handleNotificationNotFound(NotificationNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Notificação não encontrada");
        return problem;
    }
  
    @ExceptionHandler(VehicleNotOwnedByCustomerException.class)
    public ProblemDetail handleVehicleNotOwnedByCustomer(VehicleNotOwnedByCustomerException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problem.setTitle("Veículo não pertence ao cliente");
        return problem;
    }

    @ExceptionHandler(InvalidServiceOrderTransitionException.class)
    public ProblemDetail handleInvalidServiceOrderTransition(InvalidServiceOrderTransitionException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Transição de status inválida");
        return problem;
    }

    @ExceptionHandler(ServiceOrderNotFoundException.class)
    public ProblemDetail handleServiceOrderNotFound(ServiceOrderNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Ordem de serviço não encontrada");
        return problem;
    }

    @ExceptionHandler(ServiceCatalogNotFoundException.class)
    public ProblemDetail handleServiceCatalogNotFound(ServiceCatalogNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Serviço não encontrado no catalogo");
        return problem;
    }

    @ExceptionHandler(QuoteNotFoundException.class)
    public ProblemDetail handleQuoteNotFound(QuoteNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Orçamento não encontrado");
        return problem;
    }

    @ExceptionHandler(QuoteNotFoundForServiceOrderException.class)
    public ProblemDetail handleQuoteNotFoundForServiceOrder(QuoteNotFoundForServiceOrderException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Orçamento não encontrado para ordem de serviço informada");
        return problem;
    }

    @ExceptionHandler(InvalidQuoteStatusException.class)
    public ProblemDetail handleInvalidQuoteStatus(InvalidQuoteStatusException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Status do orçamento inválido");
        return problem;
    }

    @ExceptionHandler(QuoteItemAlreadyExistsException.class)
    public ProblemDetail handleQuoteItemAlreadyExists(QuoteItemAlreadyExistsException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Item já existe no orçamento");
        return problem;
    }

    @ExceptionHandler(InvalidQuoteDataException.class)
    public ProblemDetail handleInvalidQuoteData(InvalidQuoteDataException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Dados do orçamento inválidos");
        return problem;
    }

    @ExceptionHandler(InvalidQuoteItemException.class)
    public ProblemDetail handleInvalidQuoteItem(InvalidQuoteItemException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Item do orçamento inválido");
        return problem;
    }

    @ExceptionHandler(StockNotFoundException.class)
    public ProblemDetail handleStockNotFound(StockNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Estoque não encontrado");
        return problem;
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail handleInsufficientStock(InsufficientStockException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Estoque insuficiente");
        return problem;
    }

    @ExceptionHandler(QuoteAlreadyExistsException.class)
    public ProblemDetail handleQuoteAlreadyExists(QuoteAlreadyExistsException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Orçamento já existe para a ordem de serviço");
        return problem;
    }
}
