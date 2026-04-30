package br.com.ofisy.interfaces.api.user;

import br.com.ofisy.application.user.dto.CreateUserRequestDTO;
import br.com.ofisy.application.user.dto.ModifyUserRoleRequestDTO;
import br.com.ofisy.application.user.dto.UpdatePasswordRequestDTO;
import br.com.ofisy.application.user.dto.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "API de Usuários")
public interface UserApi {

    @Operation(summary = "Criar novo usuário - Roles autorizadas: (ADMIN)")
    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "401", description = "Usuário não autorizado", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "409", description = "Email já cadastrado", content = @Content(schema = @Schema(implementation = Void.class)))
    ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid CreateUserRequestDTO request);

    @Operation(summary = "Listar todos os usuários - Roles autorizadas: (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autorizado", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão", content = @Content(schema = @Schema(implementation = Void.class)))
    ResponseEntity<Page<UserResponseDTO>> listAllUsers(@ParameterObject @PageableDefault(size = 10) Pageable pageable);

    @Operation(summary = "Buscar usuário por ID - Roles autorizadas: (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Usuário encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autorizado", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = Void.class)))
    ResponseEntity<UserResponseDTO> findById(@PathVariable UUID id);

    @Operation(summary = "Alterar role do usuário - Roles autorizadas: (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Role alterada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "401", description = "Usuário não autorizado", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = Void.class)))
    ResponseEntity<UserResponseDTO> modifyRole(@PathVariable UUID id, @RequestBody @Valid ModifyUserRoleRequestDTO request);

    @Operation(summary = "Alterar senha do usuário - Roles autorizadas: (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso")
    @ApiResponse(responseCode = "400", description = "Senha atual incorreta ou dados inválidos", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "401", description = "Usuário não autorizado", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = Void.class)))
    ResponseEntity<UserResponseDTO> modifyPassword(@PathVariable UUID id, @RequestBody @Valid UpdatePasswordRequestDTO request);

    @Operation(summary = "Desativar usuário - Roles autorizadas: (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Usuário desativado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autorizado", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "409", description = "Usuário já está desativado", content = @Content(schema = @Schema(implementation = Void.class)))
    ResponseEntity<UserResponseDTO> deactivateUser(@PathVariable UUID id);

    @Operation(summary = "Ativar usuário - Roles autorizadas: (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Usuário ativado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autorizado", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = Void.class)))
    @ApiResponse(responseCode = "409", description = "Usuário já está ativo", content = @Content(schema = @Schema(implementation = Void.class)))
    ResponseEntity<UserResponseDTO> activateUser(@PathVariable UUID id);

}
