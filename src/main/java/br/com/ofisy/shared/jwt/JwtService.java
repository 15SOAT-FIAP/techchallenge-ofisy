package br.com.ofisy.shared.jwt;

import br.com.ofisy.application.user.login.TokenGenerator;
import br.com.ofisy.config.jwt.JwtProperties;
import br.com.ofisy.domain.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService implements TokenGenerator {

    private static final String ISSUER = "techchallenge-ofisy-api";
    public static final String ROLE_CLAIM = "role";

    private final JwtProperties jwtProperties;

    @Override
    public String generateToken(String email, Role role) {
        var now = Instant.now();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(email)
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.getExpiration())))
                .signWith(getSecretKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isValidToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
