package cl.usach.bda.backend.dto;

public record UsuarioAutenticacionDTO(
        Long idUsuario,
        String nombreUsuario,
        String email,
        String passwordHash,
        Boolean usuarioActivo,
        String rol
) {
}
