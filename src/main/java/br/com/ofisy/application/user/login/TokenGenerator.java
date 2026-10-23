package br.com.ofisy.application.user.login;

import br.com.ofisy.domain.user.Role;

public interface TokenGenerator {
    String generateToken(String email, Role role);
}