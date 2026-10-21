package cl.usach.bda.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoBilletera(
        Long idMovimiento,
        Long idBilletera,
        Long idViaje,
        String tipoMovimiento,
        BigDecimal monto,
        LocalDateTime fechaMovimiento
) {
}
