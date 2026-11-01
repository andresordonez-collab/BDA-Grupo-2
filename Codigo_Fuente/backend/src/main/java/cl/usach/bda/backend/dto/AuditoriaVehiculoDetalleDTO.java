package cl.usach.bda.backend.dto;

import java.time.LocalDateTime;

public record AuditoriaVehiculoDetalleDTO(
        Long idAuditoria,
        Long idVehiculo,
        String codigoVehiculo,
        Long idOperador,
        String nombreOperador,
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime fechaAuditoria,
        String motivo
) {
}
