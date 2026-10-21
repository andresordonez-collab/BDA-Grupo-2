package cl.usach.bda.backend.model;

import java.time.LocalDateTime;

public record Usuario(
        Long idUsuario,
        Long idRol,
        String nombreUsuario,
        String email,
        String passwordHash,
        Boolean usuarioActivo,
        LocalDateTime fechaRegistro
) {
}
