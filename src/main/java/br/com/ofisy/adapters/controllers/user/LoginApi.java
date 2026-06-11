package br.com.ofisy.adapters.controllers.user;

import br.com.ofisy.adapters.controllers.user.dto.LoginRequestDTO;
import br.com.ofisy.adapters.controllers.user.dto.LoginResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Autenticação via JWT Token para APIs administrativas")
public interface LoginApi {

    @Operation(summary = "Realizar login e obter o token JWT - API sem autenticação")
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso, token JWT retornado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "401", description = "Email ou senha inválidos", content = @Content(schema = @Schema(implementation = Void.class)))
    @PostMapping
    ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request);
}
