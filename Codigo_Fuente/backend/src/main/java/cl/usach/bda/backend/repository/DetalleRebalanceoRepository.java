package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.entities.DetalleRebalanceoEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DetalleRebalanceoRepository {

    private final JdbcTemplate jdbcTemplate;

    public DetalleRebalanceoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<DetalleRebalanceoEntity> DETALLES_REBALANCEO_ROW_MAPPER = (rs, rowNum) ->
            DetalleRebalanceoEntity.builder()
                    .idOrden(rs.getLong("id_orden"))
                    .idVehiculo(rs.getLong("id_vehiculo"))
                    .build();

    public List<DetalleRebalanceoEntity> findAll() {
        String sql = "SELECT * FROM detalles_rebalanceo ORDER BY id_orden ASC, id_vehiculo ASC";
        return jdbcTemplate.query(sql, DETALLES_REBALANCEO_ROW_MAPPER);
    }

    public List<DetalleRebalanceoEntity> findByOrdenId(Long idOrden) {
        String sql = "SELECT * FROM detalles_rebalanceo WHERE id_orden = ? ORDER BY id_vehiculo ASC";
        return jdbcTemplate.query(sql, DETALLES_REBALANCEO_ROW_MAPPER, idOrden);
    }

    public Optional<DetalleRebalanceoEntity> findById(Long idOrden, Long idVehiculo) {
        String sql = "SELECT * FROM detalles_rebalanceo WHERE id_orden = ? AND id_vehiculo = ?";
        return jdbcTemplate.query(sql, DETALLES_REBALANCEO_ROW_MAPPER, idOrden, idVehiculo)
                .stream()
                .findFirst();
    }

    public DetalleRebalanceoEntity save(DetalleRebalanceoEntity detalleRebalanceo) {
        String sql = "INSERT INTO detalles_rebalanceo (id_orden, id_vehiculo) VALUES (?, ?)";
        jdbcTemplate.update(sql, detalleRebalanceo.getIdOrden(), detalleRebalanceo.getIdVehiculo());
        return findById(detalleRebalanceo.getIdOrden(), detalleRebalanceo.getIdVehiculo()).orElseThrow();
    }

    public boolean deleteById(Long idOrden, Long idVehiculo) {
        String sql = "DELETE FROM detalles_rebalanceo WHERE id_orden = ? AND id_vehiculo = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idOrden, idVehiculo);
        return filasAfectadas > 0;
    }
}
