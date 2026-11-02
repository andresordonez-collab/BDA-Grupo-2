package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.dto.DisponibilidadEstacionDTO;
import cl.usach.bda.backend.dto.EstacionDetalleDTO;
import cl.usach.bda.backend.dto.VehiculoDisponibleDTO;
import cl.usach.bda.backend.entities.EstacionEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EstacionRepository {

    private final JdbcTemplate jdbcTemplate;

    public EstacionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<EstacionEntity> ESTACION_ROW_MAPPER = (rs, rowNum) ->
            EstacionEntity.builder()
                    .idEstacion(rs.getLong("id_estacion"))
                    .nombreEstacion(rs.getString("nombre_estacion"))
                    .direccion(rs.getString("direccion"))
                    .latitud(rs.getBigDecimal("latitud"))
                    .longitud(rs.getBigDecimal("longitud"))
                    .capacidad(rs.getObject("capacidad", Integer.class))
                    .estacionActiva(rs.getObject("estacion_activa", Boolean.class))
                    .build();

    public EstacionEntity save(EstacionEntity estacion){
        String sql = "INSERT INTO estaciones(" +
                "nombre_estacion, " +
                "direccion, " +
                "latitud, " +
                "longitud, " +
                "capacidad, " +
                "estacion_activa)" +
                " VALUES (?, ?, ?, ?, ?, COALESCE(?, TRUE))" +
                "RETURNING id_estacion";
        Long idGenerado = jdbcTemplate.queryForObject(sql, Long.class,
                estacion.getNombreEstacion(),
                estacion.getDireccion(),
                estacion.getLatitud(),
                estacion.getLongitud(),
                estacion.getCapacidad(),
                estacion.getEstacionActiva()
        );
        estacion.setIdEstacion(idGenerado);
        if(estacion.getEstacionActiva() == null) {
            estacion.setEstacionActiva(true);
        }
        return estacion;

    };

    public List<EstacionEntity> findAll(){
        String sql = "SELECT * FROM estaciones ORDER BY id_estacion ASC";
        return jdbcTemplate.query(sql, ESTACION_ROW_MAPPER);
    };

    public List<EstacionDetalleDTO> findAllConDetalle(){
        String sql = """
                SELECT
                    e.id_estacion,
                    e.nombre_estacion,
                    e.direccion,
                    e.latitud,
                    e.longitud,
                    e.capacidad,
                    e.estacion_activa,
                    COUNT(v.id_vehiculo) AS ocupacion,
                    COUNT(v.id_vehiculo) FILTER (
                        WHERE v.estado_vehiculo = 'Disponible'
                          AND tv.nombre_tipo_vehiculo = 'Bicicleta'
                    ) AS cantidad_bicicletas,
                    COUNT(v.id_vehiculo) FILTER (
                        WHERE v.estado_vehiculo = 'Disponible'
                          AND tv.nombre_tipo_vehiculo = 'Scooter'
                    ) AS cantidad_scooters,
                    ROUND(
                        AVG(v.nivel_bateria) FILTER (
                            WHERE v.estado_vehiculo = 'Disponible'
                              AND tv.nombre_tipo_vehiculo = 'Scooter'
                        ), 2
                    ) AS bateria_promedio
                FROM estaciones e
                LEFT JOIN vehiculos v ON v.id_estacion = e.id_estacion
                LEFT JOIN tipos_vehiculos tv ON tv.id_tipo_vehiculo = v.id_tipo_vehiculo
                GROUP BY e.id_estacion
                ORDER BY e.id_estacion
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new EstacionDetalleDTO(
                rs.getLong("id_estacion"),
                rs.getString("nombre_estacion"),
                rs.getString("direccion"),
                rs.getBigDecimal("latitud"),
                rs.getBigDecimal("longitud"),
                rs.getObject("capacidad", Integer.class),
                rs.getObject("estacion_activa", Boolean.class),
                rs.getLong("ocupacion"),
                rs.getLong("cantidad_bicicletas"),
                rs.getLong("cantidad_scooters"),
                rs.getBigDecimal("bateria_promedio")
        ));
    };

    public Optional<EstacionEntity> findById(Long idEstacion){
        String sql = "SELECT * FROM estaciones WHERE id_estacion = ?";
        return jdbcTemplate.query(sql, ESTACION_ROW_MAPPER, idEstacion)
                .stream()
                .findFirst();
    };

    public boolean update(Long idEstacion, EstacionEntity estacion){
        String sql = "UPDATE estaciones " +
                "SET nombre_estacion = ?," +
                "direccion = ?," +
                "latitud = ?," +
                "longitud = ?," +
                "capacidad = ?," +
                "estacion_activa = COALESCE(?, estacion_activa)" +
                " WHERE id_estacion = ?";
        int filasAfectadas = jdbcTemplate.update(sql,
                estacion.getNombreEstacion(),
                estacion.getDireccion(),
                estacion.getLatitud(),
                estacion.getLongitud(),
                estacion.getCapacidad(),
                estacion.getEstacionActiva(),
                idEstacion
        );
        return filasAfectadas > 0;
    };

    public boolean deleteById(Long idEstacion) {
        String sql = "DELETE FROM estaciones WHERE id_estacion = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idEstacion);
        return filasAfectadas > 0;
    };

    // Tarea 9: Calcula en la BD las bicicletas y scooters disponibles y la batería promedio por estación.
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

    public List<VehiculoDisponibleDTO> listarVehiculosDisponibles(Long idEstacion) {
        String sql = """
                SELECT
                    v.id_vehiculo,
                    v.codigo_vehiculo,
                    tv.nombre_tipo_vehiculo,
                    v.nivel_bateria
                FROM vehiculos v
                JOIN tipos_vehiculos tv ON tv.id_tipo_vehiculo = v.id_tipo_vehiculo
                WHERE v.id_estacion = ?
                  AND v.estado_vehiculo = 'Disponible'
                ORDER BY tv.nombre_tipo_vehiculo, v.codigo_vehiculo
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new VehiculoDisponibleDTO(
                rs.getLong("id_vehiculo"),
                rs.getString("codigo_vehiculo"),
                rs.getString("nombre_tipo_vehiculo"),
                rs.getObject("nivel_bateria", Integer.class)
        ), idEstacion);
    }    
}