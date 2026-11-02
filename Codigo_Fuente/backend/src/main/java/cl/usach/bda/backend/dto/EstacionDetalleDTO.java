package cl.usach.bda.backend.dto;

import java.math.BigDecimal;

/**
 * Estación con su ocupación y sus vehículos disponibles actuales.
 * Incluye los mismos campos que EstacionEntity, más los datos calculados en la BD.
 */
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
