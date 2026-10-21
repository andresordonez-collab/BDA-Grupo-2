package cl.usach.bda.backend.model;

import java.math.BigDecimal;

public record Estacion(
        Long idEstacion,
        String nombreEstacion,
        String direccion,
        BigDecimal latitud,
        BigDecimal longitud,
        Integer capacidad,
        Boolean estacionActiva
) {
}
