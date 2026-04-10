package br.com.ofisy.infrastructure.config.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String TIMESTAMP = "timestamp";
    public static final String STATUS = "status";
    public static final String ERROR = "error";
    public static final String MESSAGE = "message";

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                TIMESTAMP, LocalDateTime.now().toString(),
                STATUS, 401,
                ERROR, "Credenciais inválidas",
                MESSAGE, ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                TIMESTAMP, LocalDateTime.now().toString(),
                STATUS, 400,
                ERROR, "Requisição inválida",
                MESSAGE, ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                TIMESTAMP, LocalDateTime.now().toString(),
                STATUS, 409,
                ERROR, "Conflito de estado",
                MESSAGE, ex.getMessage()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                TIMESTAMP, LocalDateTime.now().toString(),
                STATUS, 403,
                ERROR, "Acesso Negado",
                MESSAGE, ex.getMessage()
        ));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                TIMESTAMP, LocalDateTime.now().toString(),
                STATUS, 403,
                ERROR, "Acesso Negado",
                MESSAGE, ex.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                TIMESTAMP, LocalDateTime.now().toString(),
                STATUS, 500,
                ERROR, "Erro interno",
                MESSAGE, ex.getMessage()
        ));
    }
}
