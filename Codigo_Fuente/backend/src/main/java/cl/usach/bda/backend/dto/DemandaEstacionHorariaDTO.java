package cl.usach.bda.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandaEstacionHorariaDTO {

    private Long idEstacion;
    private String nombreEstacion;
    private Integer franjaHoraria;
    private Long viajesIniciados;
    private Long viajesFinalizados;
}
