package br.com.ofisy.application.user.login;

import java.util.Collection;

public interface TokenGenerator {
    String generateToken(String email, Collection<String> roles);
}
