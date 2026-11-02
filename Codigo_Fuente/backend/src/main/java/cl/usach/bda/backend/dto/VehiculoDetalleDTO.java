package cl.usach.bda.backend.dto;

import java.time.LocalDateTime;

/**
 * Vehículo con el nombre de su tipo y de su estación.
 * Incluye los mismos campos que VehiculoEntity, más los nombres calculados en la BD.
 * nombreEstacion es null mientras el vehículo no está anclado a una estación (por ejemplo, en uso).
 */
public record VehiculoDetalleDTO(
        Long idVehiculo,
        Long idTipoVehiculo,
        String nombreTipoVehiculo,
        Long idEstacion,
        String nombreEstacion,
        String codigoVehiculo,
        Integer nivelBateria,
        LocalDateTime fechaAlta,
        String estadoVehiculo
) {
}
