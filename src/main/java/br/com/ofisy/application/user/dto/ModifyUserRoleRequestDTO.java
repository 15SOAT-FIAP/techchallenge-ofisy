package br.com.ofisy.application.user.dto;

import br.com.ofisy.domain.user.Role;
import jakarta.validation.constraints.NotNull;

public record ModifyUserRoleRequestDTO (
        @NotNull(message = "Role é obrigatória")
        Role role
) {}