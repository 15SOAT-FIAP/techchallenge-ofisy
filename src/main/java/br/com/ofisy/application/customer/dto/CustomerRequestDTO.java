package br.com.ofisy.application.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequestDTO(
        @NotBlank(message = "CPF/CNPJ é obrigatório")
        String cpfCnpj,
        @NotBlank(message = "Nome é obrigatório")
        String name,
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        String email,
        @NotBlank(message = "Telefone é obrigatório")
        String phone
) {}
