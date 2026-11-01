package cl.usach.bda.backend.dto;

import java.math.BigDecimal;

public record ResumenMensualDTO(
        Integer anio,
        Integer mes,
        Long cantidadViajes,
        Long minutosTotales,
        BigDecimal gastoTotal
) {
}
