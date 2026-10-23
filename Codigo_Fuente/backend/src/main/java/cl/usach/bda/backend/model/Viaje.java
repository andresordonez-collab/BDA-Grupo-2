package cl.usach.bda.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Viaje(
        Long idViaje,
        Long idUsuario,
        Long idVehiculo,
        Long idOrigen,
        Long idDestino,
        Long idTarifa,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Integer minutosViaje,
        BigDecimal costo,
        String estadoViaje
) {
}
