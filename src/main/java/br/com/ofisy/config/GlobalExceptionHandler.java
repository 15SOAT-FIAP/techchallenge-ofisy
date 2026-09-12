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
import br.com.ofisy.domain.customer.exceptions.InactiveCustomerException;
import br.com.ofisy.domain.customer.exceptions.InvalidCpfCnpjException;
import br.com.ofisy.domain.notification.exceptions.InvalidNotificationMessageException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteDataException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteItemException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
import br.com.ofisy.domain.user.exceptions.EmailAlreadyExistsException;
import br.com.ofisy.domain.user.exceptions.InactiveUserException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MeterRegistry meterRegistry;

    public GlobalExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    private void countError(HttpStatus status, String title, Throwable ex) {
        Counter.builder("api.errors")
                .tag("title", title)
                .tag("exception", ex.getClass().getSimpleName())
                .tag("status", String.valueOf(status.value()))
                .register(meterRegistry)
                .increment();
    }

    @ExceptionHandler({CustomerNotFoundException.class, CustomerCpfCnpjNotFoundException.class})
    public ProblemDetail handleCustomerNotFound(RuntimeException ex) {
        String title = "Cliente não encontrado";
        countError(HttpStatus.NOT_FOUND, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler({CustomerAlreadyActiveException.class, CustomerAlreadyInactiveException.class})
    public ProblemDetail handleCustomerActivationConflict(RuntimeException ex) {
        String title = "Conflito no status do cliente";
        countError(HttpStatus.CONFLICT, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(InactiveCustomerException.class)
    public ProblemDetail handleInactiveCustomer(InactiveCustomerException ex) {
        String title = "Cliente inativo";
        countError(HttpStatus.CONFLICT, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(InvalidCpfCnpjException.class)
    public ProblemDetail handleInvalidCpfCnpj(InvalidCpfCnpjException ex) {
        String title = "CPF/CNPJ inválido";
        countError(HttpStatus.BAD_REQUEST, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(InvalidNotificationMessageException.class)
    public ProblemDetail handleInvalidNotificationMessage(InvalidNotificationMessageException ex) {
        String title = "Mensagem de notificação inválida";
        countError(HttpStatus.BAD_REQUEST, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ProblemDetail handleCustomerAlreadyExists(CustomerAlreadyExistsException ex) {
        String title = "Cliente já existe";
        countError(HttpStatus.CONFLICT, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(VehicleNotFoundException.class)
    public ProblemDetail handleVehicleNotFound(VehicleNotFoundException ex) {
        String title = "Veículo não encontrado";
        countError(HttpStatus.NOT_FOUND, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(VehicleLicensePlateNotFoundException.class)
    public ProblemDetail handleVehicleLicensePlateNotFound(VehicleLicensePlateNotFoundException ex) {
        String title = "Veículo não encontrado pela placa";
        countError(HttpStatus.NOT_FOUND, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(VehicleAlreadyExistsException.class)
    public ProblemDetail handleVehicleAlreadyExists(VehicleAlreadyExistsException ex) {
        String title = "Veículo já existe";
        countError(HttpStatus.CONFLICT, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        String title = "Requisição inválida";
        countError(HttpStatus.BAD_REQUEST, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String title = "Erro de validação";
        countError(HttpStatus.BAD_REQUEST, title, ex);
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle(title);
        problem.setDetail("Um ou mais campos são inválidos");
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        String title = "Usuário não encontrado";
        countError(HttpStatus.NOT_FOUND, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        String title = "Email já cadastrado";
        countError(HttpStatus.CONFLICT, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ProblemDetail handleEmailAddressNotFound(EmailNotFoundException ex) {
        String title = "Email informado não encontrado";
        countError(HttpStatus.NOT_FOUND, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(InactiveUserException.class)
    public ProblemDetail handleDisabled(InactiveUserException ex) {
        String title = "Usuário inativo";
        countError(HttpStatus.UNAUTHORIZED, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        String title = "Não autorizado";
        countError(HttpStatus.UNAUTHORIZED, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos");
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String title = "Erro de validação";
        countError(HttpStatus.BAD_REQUEST, title, ex);
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle(title);
        problem.setDetail("Um ou mais campos são inválidos ou contêm valores não permitidos");
        return problem;
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ProblemDetail handleUsernameNotFound(UsernameNotFoundException ex) {
        String title = "Usuário não autorizado";
        countError(HttpStatus.UNAUTHORIZED, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ProblemDetail handleNotificationNotFound(NotificationNotFoundException ex) {
        String title = "Notificação não encontrada";
        countError(HttpStatus.NOT_FOUND, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(VehicleNotOwnedByCustomerException.class)
    public ProblemDetail handleVehicleNotOwnedByCustomer(VehicleNotOwnedByCustomerException ex) {
        String title = "Veículo não pertence ao cliente";
        countError(HttpStatus.UNPROCESSABLE_CONTENT, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(InvalidServiceOrderTransitionException.class)
    public ProblemDetail handleInvalidServiceOrderTransition(InvalidServiceOrderTransitionException ex) {
        String title = "Transição de status inválida";
        countError(HttpStatus.CONFLICT, title, ex);
        log.error("GlobalExceptionHandler.handleInvalidServiceOrderTransition >> {}", ex.getMessage(),
                StructuredArguments.kv("event", "order_processing_failed"));
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(ServiceOrderNotFoundException.class)
    public ProblemDetail handleServiceOrderNotFound(ServiceOrderNotFoundException ex) {
        String title = "Ordem de serviço não encontrada";
        countError(HttpStatus.NOT_FOUND, title, ex);
        log.error("GlobalExceptionHandler.handleServiceOrderNotFound >> {}", ex.getMessage(),
                StructuredArguments.kv("event", "order_processing_failed"));
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(ServiceCatalogNotFoundException.class)
    public ProblemDetail handleServiceCatalogNotFound(ServiceCatalogNotFoundException ex) {
        String title = "Serviço não encontrado no catalogo";
        countError(HttpStatus.NOT_FOUND, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(QuoteNotFoundException.class)
    public ProblemDetail handleQuoteNotFound(QuoteNotFoundException ex) {
        String title = "Orçamento não encontrado";
        countError(HttpStatus.NOT_FOUND, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(QuoteNotFoundForServiceOrderException.class)
    public ProblemDetail handleQuoteNotFoundForServiceOrder(QuoteNotFoundForServiceOrderException ex) {
        String title = "Orçamento não encontrado para ordem de serviço informada";
        countError(HttpStatus.NOT_FOUND, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(InvalidQuoteStatusException.class)
    public ProblemDetail handleInvalidQuoteStatus(InvalidQuoteStatusException ex) {
        String title = "Status do orçamento inválido";
        countError(HttpStatus.CONFLICT, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(QuoteItemAlreadyExistsException.class)
    public ProblemDetail handleQuoteItemAlreadyExists(QuoteItemAlreadyExistsException ex) {
        String title = "Item já existe no orçamento";
        countError(HttpStatus.CONFLICT, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(InvalidQuoteDataException.class)
    public ProblemDetail handleInvalidQuoteData(InvalidQuoteDataException ex) {
        String title = "Dados do orçamento inválidos";
        countError(HttpStatus.BAD_REQUEST, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(InvalidQuoteItemException.class)
    public ProblemDetail handleInvalidQuoteItem(InvalidQuoteItemException ex) {
        String title = "Item do orçamento inválido";
        countError(HttpStatus.BAD_REQUEST, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(StockNotFoundException.class)
    public ProblemDetail handleStockNotFound(StockNotFoundException ex) {
        String title = "Estoque não encontrado";
        countError(HttpStatus.NOT_FOUND, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail handleInsufficientStock(InsufficientStockException ex) {
        String title = "Estoque insuficiente";
        countError(HttpStatus.CONFLICT, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }

    @ExceptionHandler(QuoteAlreadyExistsException.class)
    public ProblemDetail handleQuoteAlreadyExists(QuoteAlreadyExistsException ex) {
        String title = "Orçamento já existe para a ordem de serviço";
        countError(HttpStatus.CONFLICT, title, ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle(title);
        return problem;
    }
}
