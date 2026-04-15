package br.com.ofisy.application.customer.dto;

import java.time.LocalDateTime;

public record CustomerResponseDTO(
        String cpfCnpj,
        String name,
        String email,
        String phone,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
