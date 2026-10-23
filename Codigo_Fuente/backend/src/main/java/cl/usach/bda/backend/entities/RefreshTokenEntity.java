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
public class RefreshTokenEntity {

    private Long idRefreshToken;
    private Long idUsuario;
    private String tokenHash;
    private LocalDateTime expiraEn;
    private Boolean revocado;
}
