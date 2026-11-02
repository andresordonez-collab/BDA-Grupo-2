package cl.usach.bda.backend.dto;

import java.time.LocalDateTime;

/**
 * Orden de rebalanceo con el desglose por tipo de los vehículos trasladados.
 * Incluye los mismos campos que OrdenRebalanceoEntity, más los datos calculados en la BD.
 */
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
