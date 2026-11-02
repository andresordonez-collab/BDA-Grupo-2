package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.entities.TipoVehiculoEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TipoVehiculoRepository {

    private final JdbcTemplate jdbcTemplate;

    public TipoVehiculoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<TipoVehiculoEntity> TIPOS_VEHICULOS_ROW_MAPPER = (rs, rowNum) ->
            TipoVehiculoEntity.builder()
                    .idTipoVehiculo(rs.getLong("id_tipo_vehiculo"))
                    .nombreTipoVehiculo(rs.getString("nombre_tipo_vehiculo"))
                    .build();

    public TipoVehiculoEntity save(TipoVehiculoEntity tipoVehiculo) {
        String sql = "INSERT INTO tipos_vehiculos (nombre_tipo_vehiculo)"
                + " VALUES (?) RETURNING id_tipo_vehiculo";
        Long idGenerado = jdbcTemplate.queryForObject(sql, Long.class,
                tipoVehiculo.getNombreTipoVehiculo()
        );
        tipoVehiculo.setIdTipoVehiculo(idGenerado);
        return tipoVehiculo;
    }

    public List<TipoVehiculoEntity> findAll() {
        String sql = "SELECT * FROM tipos_vehiculos ORDER BY id_tipo_vehiculo ASC";
        return jdbcTemplate.query(sql, TIPOS_VEHICULOS_ROW_MAPPER);
    }

    public Optional<TipoVehiculoEntity> findById(Long idTipoVehiculo) {
        String sql = "SELECT * FROM tipos_vehiculos WHERE id_tipo_vehiculo = ?";
        return jdbcTemplate.query(sql, TIPOS_VEHICULOS_ROW_MAPPER, idTipoVehiculo)
                .stream()
                .findFirst();
    }

    public boolean update(Long idTipoVehiculo, TipoVehiculoEntity tipoVehiculo) {
        String sql = "UPDATE tipos_vehiculos SET nombre_tipo_vehiculo = ?"
                + " WHERE id_tipo_vehiculo = ?";
        int filasAfectadas = jdbcTemplate.update(sql,
                tipoVehiculo.getNombreTipoVehiculo(),
                idTipoVehiculo
        );
        return filasAfectadas > 0;
    }

    public boolean deleteById(Long idTipoVehiculo) {
        String sql = "DELETE FROM tipos_vehiculos WHERE id_tipo_vehiculo = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idTipoVehiculo);
        return filasAfectadas > 0;
    }
}
