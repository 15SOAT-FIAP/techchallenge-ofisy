package br.com.ofisy.shared.jwt;

public interface TokenGenerator {
    String generateToken(String email);
}
