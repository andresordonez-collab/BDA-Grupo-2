package cl.usach.bda.backend.dto;

public record SesionDTO(
        Long idUsuario,
        String nombreUsuario,
        String email,
        String rol
) {
}
