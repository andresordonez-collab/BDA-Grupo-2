package cl.usach.bda.backend.model;

import java.time.LocalDateTime;

public record OrdenRebalanceo(
        Long idOrden,
        Long idEstacionOrigen,
        Long idEstacionDestino,
        Long idOperador,
        Integer cantidadVehiculos,
        LocalDateTime fechaOrden,
        String estadoOrden
) {
}
