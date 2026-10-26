package cl.usach.bda.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadEstacionDTO {

    private Long idEstacion;
    private String nombreEstacion;
    private Long cantidadBicicletas;
    private Long cantidadScooters;
    private BigDecimal bateriaPromedio;
}