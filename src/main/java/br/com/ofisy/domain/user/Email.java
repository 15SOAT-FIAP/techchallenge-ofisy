package br.com.ofisy.domain.user;

import br.com.ofisy.domain.user.exceptions.InvalidEmailException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Email(
        @Column(name = "email", nullable = false, unique = true)
        String emailAddress
) {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public Email {

        if (!isValid(emailAddress)) {
            throw new InvalidEmailException("E-mail informado é inválido!");
        }
        emailAddress = emailAddress.toLowerCase().trim();
    }

    public static boolean isValid(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }
}