package cl.usach.bda.backend.dto;

/** Datos de un usuario necesarios para autenticarlo.*/
public record UsuarioAutenticacionDTO(
        Long idUsuario,
        String nombreUsuario,
        String email,
        String passwordHash,
        Boolean usuarioActivo,
        String rol
) {
}
