package br.com.ofisy.interfaces.api.user;

import br.com.ofisy.application.user.dto.UserDTO.*;
import br.com.ofisy.application.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController()
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuários")
@SecurityRequirement(name = "Bearer Token")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Criar novo usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> listAllUsers() {
        return ResponseEntity.ok(userService.listAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        UserResponse userResponse = userService.findById(id);
        return ResponseEntity.ok(userResponse);
    }

    @PatchMapping("/{id}/modify-role")
    @Operation(summary = "Alterar role do usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> modifyRole(
            @PathVariable UUID id,
            @RequestBody @Valid ModifyUserRoleRequest request) {
        return ResponseEntity.ok(userService.modifyUserRole(id, request));
    }

    @PatchMapping("/{id}/update-password")
    @Operation(summary = "Alterar senha do usuário")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponse> modifyPassword(
            @PathVariable UUID id,
            @RequestBody @Valid UpdatePasswordRequest request) {
        return ResponseEntity.ok(userService.updatePassword(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativar usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Ativar usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> activateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    @DeleteMapping("/{id}/remove")
    @Operation(summary = "Remover usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> removeUser(@PathVariable UUID id) {
        userService.removeUser(id);
        return ResponseEntity.noContent().build();
    }
}
