package br.com.ofisy.adapters.controllers.customer.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponseDTO(
        UUID id,
        String cpfCnpj,
        String name,
        String email,
        String phone,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
