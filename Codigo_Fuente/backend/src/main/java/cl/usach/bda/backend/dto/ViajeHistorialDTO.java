package cl.usach.bda.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ViajeHistorialDTO(
        Long idViaje,
        String codigoVehiculo,
        String tipoVehiculo,
        String estacionOrigen,
        String estacionDestino,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Integer minutosViaje,
        BigDecimal costo,
        String estadoViaje
) {
}
