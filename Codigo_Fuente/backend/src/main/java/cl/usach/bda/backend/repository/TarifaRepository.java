package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.entities.TarifaEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TarifaRepository {

    private final JdbcTemplate jdbcTemplate;

    public TarifaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Row mapper para mapear las filas de la tabla a la entidad.
    private static final RowMapper<TarifaEntity> TARIFAS_ROW_MAPPER = (rs, rowNum) ->
            TarifaEntity.builder()
                    .idTarifa(rs.getLong("id_tarifa"))
                    .idTipoVehiculo(rs.getLong("id_tipo_vehiculo"))
                    .valorDesbloqueo(rs.getBigDecimal("valor_desbloqueo"))
                    .valorMinuto(rs.getBigDecimal("valor_minuto"))
                    .vigenteDesde(rs.getObject("vigente_desde", LocalDateTime.class))
                    .activa(rs.getObject("activa", Boolean.class))
                    .build();

    // Crear un registro y obtener el ID generado por PostgreSQL.
    public TarifaEntity save(TarifaEntity tarifa) {
        String sql = "INSERT INTO tarifas (id_tipo_vehiculo, valor_desbloqueo, valor_minuto, vigente_desde, activa)"
                + " VALUES (?, COALESCE(?, 0.00), ?, COALESCE(?, LOCALTIMESTAMP), COALESCE(?, TRUE)) RETURNING id_tarifa";
        Long idGenerado = jdbcTemplate.queryForObject(sql, Long.class,
                tarifa.getIdTipoVehiculo(),
                tarifa.getValorDesbloqueo(),
                tarifa.getValorMinuto(),
                tarifa.getVigenteDesde(),
                tarifa.getActiva()
        );
        tarifa.setIdTarifa(idGenerado);
        // Leer los valores predeterminados aplicados por PostgreSQL.
        return findById(idGenerado).orElseThrow();
    }

    // Obtener todos los registros.
    public List<TarifaEntity> findAll() {
        String sql = "SELECT * FROM tarifas ORDER BY id_tarifa ASC";
        return jdbcTemplate.query(sql, TARIFAS_ROW_MAPPER);
    }

    // Obtener un registro por su ID.
    public Optional<TarifaEntity> findById(Long idTarifa) {
        String sql = "SELECT * FROM tarifas WHERE id_tarifa = ?";
        return jdbcTemplate.query(sql, TARIFAS_ROW_MAPPER, idTarifa)
                .stream()
                .findFirst();
    }

    // Actualizar un registro existente.
    public boolean update(Long idTarifa, TarifaEntity tarifa) {
        String sql = "UPDATE tarifas SET id_tipo_vehiculo = ?, valor_desbloqueo = COALESCE(?, valor_desbloqueo), valor_minuto = ?, vigente_desde = COALESCE(?, vigente_desde), activa = COALESCE(?, activa)"
                + " WHERE id_tarifa = ?";
        int filasAfectadas = jdbcTemplate.update(sql,
                tarifa.getIdTipoVehiculo(),
                tarifa.getValorDesbloqueo(),
                tarifa.getValorMinuto(),
                tarifa.getVigenteDesde(),
                tarifa.getActiva(),
                idTarifa
        );
        return filasAfectadas > 0;
    }

    // Eliminar un registro por su ID.
    public boolean deleteById(Long idTarifa) {
        String sql = "DELETE FROM tarifas WHERE id_tarifa = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idTarifa);
        return filasAfectadas > 0;
    }
}
