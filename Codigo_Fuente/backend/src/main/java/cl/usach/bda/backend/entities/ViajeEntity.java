package cl.usach.bda.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViajeEntity {

    private Long idViaje;
    private Long idUsuario;
    private Long idVehiculo;
    private Long idOrigen;
    private Long idDestino;
    private Long idTarifa;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer minutosViaje;
    private BigDecimal costo;
    private String estadoViaje;
}
