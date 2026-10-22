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
public class OrdenRebalanceoEntity {

    private Long idOrden;
    private Long idEstacionOrigen;
    private Long idEstacionDestino;
    private Long idOperador;
    private Integer cantidadVehiculos;
    private LocalDateTime fechaOrden;
    private String estadoOrden;
}
