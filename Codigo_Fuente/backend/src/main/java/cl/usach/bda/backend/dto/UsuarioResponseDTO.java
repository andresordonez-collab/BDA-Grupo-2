package cl.usach.bda.backend.dto;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long idUsuario,
        Long idRol,
        String nombreUsuario,
        String email,
        Boolean usuarioActivo,
        LocalDateTime fechaRegistro
) {}
