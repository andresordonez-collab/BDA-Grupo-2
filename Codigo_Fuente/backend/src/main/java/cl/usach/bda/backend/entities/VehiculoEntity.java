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
public class VehiculoEntity {

    private Long idVehiculo;
    private Long idTipoVehiculo;
    private Long idEstacion;
    private String codigoVehiculo;
    private Integer nivelBateria;
    private LocalDateTime fechaAlta;
    private String estadoVehiculo;
}
