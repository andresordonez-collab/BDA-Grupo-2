package cl.usach.bda.backend.model;

import java.math.BigDecimal;

public record Billetera(
        Long idBilletera,
        Long idUsuario,
        BigDecimal saldo
) {
}
