package br.com.ofisy.interfaces.api.user;

import br.com.ofisy.application.user.UserService;
import br.com.ofisy.application.user.dto.CreateUserRequestDTO;
import br.com.ofisy.application.user.dto.ModifyUserRoleRequestDTO;
import br.com.ofisy.application.user.dto.UpdatePasswordRequestDTO;
import br.com.ofisy.application.user.dto.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController()
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "CRUD Usuários")
@SecurityRequirement(name = "Bearer Token")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Criar novo usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid CreateUserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> listAllUsers(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.listAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PatchMapping("/{id}/modify-role")
    @Operation(summary = "Alterar role do usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> modifyRole(
            @PathVariable UUID id,
            @RequestBody @Valid ModifyUserRoleRequestDTO request) {
        return ResponseEntity.ok(userService.modifyUserRole(id, request));
    }

    @PatchMapping("/{id}/update-password")
    @Operation(summary = "Alterar senha do usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> modifyPassword(
            @PathVariable UUID id,
            @RequestBody @Valid UpdatePasswordRequestDTO request) {
        return ResponseEntity.ok(userService.updatePassword(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativar usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> deactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Ativar usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> activateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    @DeleteMapping("/{id}/remove")
    @Operation(summary = "Remover usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeUser(@PathVariable UUID id) {
        userService.removeUser(id);
        return ResponseEntity.noContent().build();
    }
}
