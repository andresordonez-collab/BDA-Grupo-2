package cl.usach.bda.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank String email,
        String password,
        String passwordCifrada
) {
}
