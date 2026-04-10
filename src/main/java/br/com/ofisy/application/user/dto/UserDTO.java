package br.com.ofisy.application.user.dto;

import br.com.ofisy.domain.user.Email;
import br.com.ofisy.domain.user.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class UserDTO {

    public record CreateUserRequest(
            @NotBlank(message = "Nome é obrigatório")
            String name,

            @NotBlank(message = "Email é obrigatório")
            @jakarta.validation.constraints.Email(message = "Email inválido")
            String email,

            @NotBlank(message = "Senha é obrigatória")
            @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
            String password,

            @NotNull(message = "Role é obrigatória")
            Role role
    ) {}

    public record UpdatePasswordRequest(
            @NotBlank(message = "Senha atual é obrigatória")
            String currentPassword,

            @NotBlank(message = "Nova senha é obrigatória")
            @Size(min = 8, message = "Nova senha deve ter no mínimo 8 caracteres")
            String newPassword
    ) {}

    public record ModifyUserRoleRequest (
            @NotNull(message = "Role é obrigatória")
            Role role
    ) {}

    public record UserResponse(
            UUID id,
            String name,
            Email email,
            Role role,
            boolean active,
            LocalDateTime creationDate,
            LocalDateTime modifiedDate
    ) {}
}