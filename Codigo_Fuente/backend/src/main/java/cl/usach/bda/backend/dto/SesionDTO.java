package cl.usach.bda.backend.dto;

/** Datos del usuario autenticado que se devuelven al cliente. NO incluye el hash de la contraseña. */
public record SesionDTO(
        Long idUsuario,
        String nombreUsuario,
        String email,
        String rol
) {
}
