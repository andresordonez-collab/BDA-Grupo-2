package cl.usach.bda.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity {

    private Long idUsuario;
    private Long idRol;
    private String nombreUsuario;
    private String email;
    private String passwordHash;
    private Boolean usuarioActivo;
    private LocalDateTime fechaRegistro;
}
