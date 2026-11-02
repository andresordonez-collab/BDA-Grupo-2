package cl.usach.bda.backend.dto;

import java.time.LocalDateTime;

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
