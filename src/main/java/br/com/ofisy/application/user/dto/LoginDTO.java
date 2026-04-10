package br.com.ofisy.application.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class LoginDTO {

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String password
    ) {}

    public record LoginResponse(String token) {}
}
