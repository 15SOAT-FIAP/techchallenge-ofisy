package br.com.ofisy.application.user.dto;

import br.com.ofisy.domain.user.Email;
import br.com.ofisy.domain.user.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO (
        UUID id,
        String name,
        Email email,
        Role role,
        boolean active,
        LocalDateTime creationDate,
        LocalDateTime modifiedDate
) {}
