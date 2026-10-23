package cl.usach.bda.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaVehiculoEntity {

    private Long idAuditoria;
    private Long idVehiculo;
    private Long idOperador;
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime fechaAuditoria;
    private String motivo;
}
