package br.com.ofisy.adapters.controllers.user.dto;

import br.com.ofisy.domain.user.Role;
import jakarta.validation.constraints.NotNull;

public record ModifyUserRoleRequestDTO(
        @NotNull(message = "Role é obrigatória")
        Role role
) {}
