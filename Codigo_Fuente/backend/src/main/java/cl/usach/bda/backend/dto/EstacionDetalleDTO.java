package cl.usach.bda.backend.dto;

import java.math.BigDecimal;

public record EstacionDetalleDTO(
        Long idEstacion,
        String nombreEstacion,
        String direccion,
        BigDecimal latitud,
        BigDecimal longitud,
        Integer capacidad,
        Boolean estacionActiva,
        Long ocupacion,
        Long cantidadBicicletas,
        Long cantidadScooters,
        BigDecimal bateriaPromedio
) {
}
