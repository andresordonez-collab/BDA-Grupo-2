package cl.usach.bda.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoBilleteraResumenDTO(
        Long idMovimiento,
        String tipoMovimiento,
        BigDecimal monto,
        LocalDateTime fechaMovimiento,
        Long idViaje
) {
}
