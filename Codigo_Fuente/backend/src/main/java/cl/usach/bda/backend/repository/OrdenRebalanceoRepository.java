package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.dto.OrdenRebalanceoDetalleDTO;
import cl.usach.bda.backend.entities.OrdenRebalanceoEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class OrdenRebalanceoRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrdenRebalanceoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<OrdenRebalanceoEntity> ORDENES_REBALANCEO_ROW_MAPPER = (rs, rowNum) ->
            OrdenRebalanceoEntity.builder()
                    .idOrden(rs.getLong("id_orden"))
                    .idEstacionOrigen(rs.getLong("id_estacion_origen"))
                    .idEstacionDestino(rs.getLong("id_estacion_destino"))
                    .idOperador(rs.getLong("id_operador"))
                    .cantidadVehiculos(rs.getObject("cantidad_vehiculos", Integer.class))
                    .fechaOrden(rs.getObject("fecha_orden", LocalDateTime.class))
                    .estadoOrden(rs.getString("estado_orden"))
                    .build();

    public OrdenRebalanceoEntity save(OrdenRebalanceoEntity ordenRebalanceo) {
        String sql = "INSERT INTO ordenes_rebalanceo (id_estacion_origen, id_estacion_destino, id_operador, cantidad_vehiculos, fecha_orden, estado_orden)"
                + " VALUES (?, ?, ?, ?, COALESCE(?, LOCALTIMESTAMP), ?) RETURNING id_orden";
        Long idGenerado = jdbcTemplate.queryForObject(sql, Long.class,
                ordenRebalanceo.getIdEstacionOrigen(),
                ordenRebalanceo.getIdEstacionDestino(),
                ordenRebalanceo.getIdOperador(),
                ordenRebalanceo.getCantidadVehiculos(),
                ordenRebalanceo.getFechaOrden(),
                ordenRebalanceo.getEstadoOrden()
        );
        ordenRebalanceo.setIdOrden(idGenerado);
        return findById(idGenerado).orElseThrow();
    }

    public List<OrdenRebalanceoEntity> findAll() {
        String sql = "SELECT * FROM ordenes_rebalanceo ORDER BY id_orden ASC";
        return jdbcTemplate.query(sql, ORDENES_REBALANCEO_ROW_MAPPER);
    }

    // Órdenes con el desglose por tipo de los vehículos trasladados (según detalles_rebalanceo).
    public List<OrdenRebalanceoDetalleDTO> findAllConDetalle() {
        String sql = """
                SELECT
                    o.id_orden,
                    o.id_estacion_origen,
                    o.id_estacion_destino,
                    o.id_operador,
                    o.cantidad_vehiculos,
                    o.fecha_orden,
                    o.estado_orden,
                    COUNT(d.id_vehiculo) FILTER (
                        WHERE tv.nombre_tipo_vehiculo = 'Bicicleta'
                    ) AS cantidad_bicicletas,
                    COUNT(d.id_vehiculo) FILTER (
                        WHERE tv.nombre_tipo_vehiculo = 'Scooter'
                    ) AS cantidad_scooters
                FROM ordenes_rebalanceo o
                LEFT JOIN detalles_rebalanceo d ON d.id_orden = o.id_orden
                LEFT JOIN vehiculos v ON v.id_vehiculo = d.id_vehiculo
                LEFT JOIN tipos_vehiculos tv ON tv.id_tipo_vehiculo = v.id_tipo_vehiculo
                GROUP BY o.id_orden
                ORDER BY o.id_orden ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new OrdenRebalanceoDetalleDTO(
                rs.getLong("id_orden"),
                rs.getLong("id_estacion_origen"),
                rs.getLong("id_estacion_destino"),
                rs.getLong("id_operador"),
                rs.getObject("cantidad_vehiculos", Integer.class),
                rs.getObject("fecha_orden", LocalDateTime.class),
                rs.getString("estado_orden"),
                rs.getLong("cantidad_bicicletas"),
                rs.getLong("cantidad_scooters")
        ));
    }

    public Optional<OrdenRebalanceoEntity> findById(Long idOrden) {
        String sql = "SELECT * FROM ordenes_rebalanceo WHERE id_orden = ?";
        return jdbcTemplate.query(sql, ORDENES_REBALANCEO_ROW_MAPPER, idOrden)
                .stream()
                .findFirst();
    }

    public boolean update(Long idOrden, OrdenRebalanceoEntity ordenRebalanceo) {
        String sql = "UPDATE ordenes_rebalanceo SET id_estacion_origen = ?, id_estacion_destino = ?, id_operador = ?, cantidad_vehiculos = ?, fecha_orden = COALESCE(?, fecha_orden), estado_orden = ?"
                + " WHERE id_orden = ?";
        int filasAfectadas = jdbcTemplate.update(sql,
                ordenRebalanceo.getIdEstacionOrigen(),
                ordenRebalanceo.getIdEstacionDestino(),
                ordenRebalanceo.getIdOperador(),
                ordenRebalanceo.getCantidadVehiculos(),
                ordenRebalanceo.getFechaOrden(),
                ordenRebalanceo.getEstadoOrden(),
                idOrden
        );
        return filasAfectadas > 0;
    }

    public boolean deleteById(Long idOrden) {
        String sql = "DELETE FROM ordenes_rebalanceo WHERE id_orden = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idOrden);
        return filasAfectadas > 0;
    }
}
