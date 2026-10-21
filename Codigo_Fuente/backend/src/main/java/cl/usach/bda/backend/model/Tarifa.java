package cl.usach.bda.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Tarifa(
        Long idTarifa,
        Long idTipoVehiculo,
        BigDecimal valorDesbloqueo,
        BigDecimal valorMinuto,
        LocalDateTime vigenteDesde,
        Boolean activa
) {
}
