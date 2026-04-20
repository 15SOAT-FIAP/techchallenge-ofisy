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

        if (emailAddress != null) {
            emailAddress = emailAddress.toLowerCase().trim();
        }

        if (!isValid(emailAddress)) {
            throw new InvalidEmailException("E-mail informado é inválido!");
        }
    }

    public static boolean isValid(String email) {
        if (email == null) {
            return false;
        }

        return email.trim().toLowerCase().matches(EMAIL_REGEX);
    }
}