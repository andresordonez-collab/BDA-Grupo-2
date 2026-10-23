package cl.usach.bda.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstacionEntity {

    private Long idEstacion;
    private String nombreEstacion;
    private String direccion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Integer capacidad;
    private Boolean estacionActiva;
}
