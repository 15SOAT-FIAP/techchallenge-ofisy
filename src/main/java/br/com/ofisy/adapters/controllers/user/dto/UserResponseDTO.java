package br.com.ofisy.adapters.controllers.user.dto;

import br.com.ofisy.domain.user.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        Role role,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
