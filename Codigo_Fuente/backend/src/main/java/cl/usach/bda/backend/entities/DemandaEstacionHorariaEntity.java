package cl.usach.bda.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaEstacionHorariaEntity {

    private Long idEstacion;
    private String nombreEstacion;
    private Integer franjaHoraria;
    private Long viajesIniciados;     // COUNT(*) devuelve bigint
    private Long viajesFinalizados;
}
