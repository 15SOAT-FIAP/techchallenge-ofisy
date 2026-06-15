package br.com.ofisy.application.user.login;

public interface TokenGenerator {
    String generateToken(String email);
}
