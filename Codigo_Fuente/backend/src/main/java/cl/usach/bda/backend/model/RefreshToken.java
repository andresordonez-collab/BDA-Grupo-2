package cl.usach.bda.backend.model;

import java.time.LocalDateTime;

public record RefreshToken(
        Long idRefreshToken,
        Long idUsuario,
        String tokenHash,
        LocalDateTime expiraEn,
        Boolean revocado
) {
}
