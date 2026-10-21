package cl.usach.bda.backend.model;

import java.time.LocalDateTime;

public record Vehiculo(
        Long idVehiculo,
        Long idTipoVehiculo,
        Long idEstacion,
        String codigoVehiculo,
        Integer nivelBateria,
        LocalDateTime fechaAlta,
        String estadoVehiculo
) {
}
