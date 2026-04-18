package br.com.ofisy.interfaces.api.user;

import br.com.ofisy.application.user.dto.LoginRequestDTO;
import br.com.ofisy.application.user.dto.LoginResponseDTO;
import br.com.ofisy.infrastructure.config.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
@Tag(name = "Autenticação via JWT Token para APIs administrativas")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping
    @Operation(summary = "Realizar login e obter o token JWT")
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso, token JWT retornado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "401", description = "Email ou senha inválidos", content = @Content(schema = @Schema(implementation = Void.class)))
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        String token = jwtService.generateToken(authentication.getName());
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}