package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.dto.DisponibilidadEstacionDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EstacionRepository {

    private final JdbcTemplate jdbcTemplate;

    public EstacionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DisponibilidadEstacionDTO> obtenerDisponibilidad() {

        String sql = """
                SELECT
                    e.id_estacion,
                    e.nombre_estacion,
                    COUNT(*) FILTER (
                        WHERE tv.nombre_tipo_vehiculo = 'Bicicleta'
                    ) AS cantidad_bicicletas,
                    COUNT(*) FILTER (
                        WHERE tv.nombre_tipo_vehiculo = 'Scooter'
                    ) AS cantidad_scooters,
                    ROUND(
                        AVG(v.nivel_bateria) FILTER (
                            WHERE tv.nombre_tipo_vehiculo = 'Scooter'
                        ), 2
                    ) AS bateria_promedio
                FROM estaciones e
                JOIN vehiculos v
                    ON e.id_estacion = v.id_estacion
                JOIN tipos_vehiculos tv
                    ON v.id_tipo_vehiculo = tv.id_tipo_vehiculo
                WHERE v.estado_vehiculo = 'Disponible'
                GROUP BY e.id_estacion, e.nombre_estacion
                ORDER BY e.id_estacion
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new DisponibilidadEstacionDTO(
                        rs.getLong("id_estacion"),
                        rs.getString("nombre_estacion"),
                        rs.getLong("cantidad_bicicletas"),
                        rs.getLong("cantidad_scooters"),
                        rs.getBigDecimal("bateria_promedio")
                )
        );
    }
}