package cl.usach.bda.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record MiBilleteraDTO(
        Long idBilletera,
        BigDecimal saldo,
        List<MovimientoBilleteraResumenDTO> movimientos
) {
}
