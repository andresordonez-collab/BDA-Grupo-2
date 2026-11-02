package cl.usach.bda.backend.dto;

import java.time.LocalDateTime;

public record OrdenRebalanceoDetalleDTO(
        Long idOrden,
        Long idEstacionOrigen,
        Long idEstacionDestino,
        Long idOperador,
        Integer cantidadVehiculos,
        LocalDateTime fechaOrden,
        String estadoOrden,
        Long cantidadBicicletas,
        Long cantidadScooters
) {
}
