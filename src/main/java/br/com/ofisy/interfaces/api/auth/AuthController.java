package br.com.ofisy.interfaces.api.auth;

import br.com.ofisy.application.auth.service.JwtService;
import br.com.ofisy.application.user.dto.LoginDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Autenticação via JWT Token para APIs administrativas")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Realizar login e obter o token JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            if (!authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = jwtService.generateToken(authentication.getName());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (BadCredentialsException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("E-mail ou senha inválidos."));
        }
    }
}