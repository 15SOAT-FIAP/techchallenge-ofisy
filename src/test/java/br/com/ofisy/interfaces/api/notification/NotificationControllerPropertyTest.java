package br.com.ofisy.interfaces.api.notification;

import br.com.ofisy.application.notification.dto.NotificationRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.jqwik.api.*;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Feature: notification-stock-monitor, Propriedade 4: blankMessageIsRejected
class NotificationControllerPropertyTest {

    private final Validator validator;

    NotificationControllerPropertyTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    /**
     * Propriedade 4: Validação rejeita mensagens em branco
     * Valida: Requisito 2.4
     *
     * Para qualquer string composta inteiramente de espaços em branco (incluindo string vazia),
     * a validação do DTO deve rejeitar a requisição com violação no campo "message".
     */
    @Property(tries = 100)
    void blankMessageIsRejected(@ForAll("blankMessages") String blankMessage) {
        // Arrange
        NotificationRequestDTO request = new NotificationRequestDTO(UUID.randomUUID(), blankMessage);

        // Act
        Set<ConstraintViolation<NotificationRequestDTO>> violations = validator.validate(request);

        // Assert
        assertThat(violations)
                .as("Mensagem em branco '%s' deve gerar violação de validação", blankMessage)
                .isNotEmpty();

        boolean hasMessageViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("message"));

        assertThat(hasMessageViolation)
                .as("A violação deve ser no campo 'message' para entrada: '%s'", blankMessage)
                .isTrue();
    }

    @Provide
    Arbitrary<String> blankMessages() {
        // Generate strings composed entirely of whitespace characters, including empty string
        Arbitrary<String> emptyString = Arbitraries.just("");
        Arbitrary<String> whitespaceOnly = Arbitraries.strings()
                .withChars(' ', '\t', '\n', '\r')
                .ofMinLength(1)
                .ofMaxLength(20);
        return Arbitraries.oneOf(emptyString, whitespaceOnly);
    }
}
