package cl.usach.bda.backend.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String passwordHash;
    private Boolean usuarioActivo;
    private LocalDateTime fechaRegistro;
}
