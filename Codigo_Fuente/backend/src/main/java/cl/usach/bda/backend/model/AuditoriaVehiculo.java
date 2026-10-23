package cl.usach.bda.backend.model;

import java.time.LocalDateTime;

public record AuditoriaVehiculo(
        Long idAuditoria,
        Long idVehiculo,
        Long idOperador,
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime fechaAuditoria,
        String motivo
) {
}
