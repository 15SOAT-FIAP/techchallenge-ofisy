package br.com.ofisy.interfaces.api.customer.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponseDTO(
        UUID id,
        String cpfCnpj,
        String name,
        String email,
        String phone,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
