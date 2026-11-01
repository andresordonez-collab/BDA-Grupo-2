package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.entities.AuditoriaVehiculoEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AuditoriaVehiculoRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuditoriaVehiculoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<AuditoriaVehiculoEntity> AUDITORIAS_VEHICULOS_ROW_MAPPER = (rs, rowNum) ->
            AuditoriaVehiculoEntity.builder()
                    .idAuditoria(rs.getLong("id_auditoria"))
                    .idVehiculo(rs.getLong("id_vehiculo"))
                    .idOperador(rs.getLong("id_operador"))
                    .estadoAnterior(rs.getString("estado_anterior"))
                    .estadoNuevo(rs.getString("estado_nuevo"))
                    .fechaAuditoria(rs.getObject("fecha_auditoria", LocalDateTime.class))
                    .motivo(rs.getString("motivo"))
                    .build();

    public AuditoriaVehiculoEntity save(AuditoriaVehiculoEntity auditoriaVehiculo) {
        String sql = "INSERT INTO auditorias_vehiculos (id_vehiculo, id_operador, estado_anterior, estado_nuevo, fecha_auditoria, motivo)"
                + " VALUES (?, ?, ?, ?, COALESCE(?, LOCALTIMESTAMP), ?) RETURNING id_auditoria";
        Long idGenerado = jdbcTemplate.queryForObject(sql, Long.class,
                auditoriaVehiculo.getIdVehiculo(),
                auditoriaVehiculo.getIdOperador(),
                auditoriaVehiculo.getEstadoAnterior(),
                auditoriaVehiculo.getEstadoNuevo(),
                auditoriaVehiculo.getFechaAuditoria(),
                auditoriaVehiculo.getMotivo()
        );
        auditoriaVehiculo.setIdAuditoria(idGenerado);
        return findById(idGenerado).orElseThrow();
    }

    public List<AuditoriaVehiculoEntity> findAll() {
        String sql = "SELECT * FROM auditorias_vehiculos ORDER BY id_auditoria ASC";
        return jdbcTemplate.query(sql, AUDITORIAS_VEHICULOS_ROW_MAPPER);
    }

    public Optional<AuditoriaVehiculoEntity> findById(Long idAuditoria) {
        String sql = "SELECT * FROM auditorias_vehiculos WHERE id_auditoria = ?";
        return jdbcTemplate.query(sql, AUDITORIAS_VEHICULOS_ROW_MAPPER, idAuditoria)
                .stream()
                .findFirst();
    }

    public boolean update(Long idAuditoria, AuditoriaVehiculoEntity auditoriaVehiculo) {
        String sql = "UPDATE auditorias_vehiculos SET id_vehiculo = ?, id_operador = ?, estado_anterior = ?, estado_nuevo = ?, fecha_auditoria = COALESCE(?, fecha_auditoria), motivo = ?"
                + " WHERE id_auditoria = ?";
        int filasAfectadas = jdbcTemplate.update(sql,
                auditoriaVehiculo.getIdVehiculo(),
                auditoriaVehiculo.getIdOperador(),
                auditoriaVehiculo.getEstadoAnterior(),
                auditoriaVehiculo.getEstadoNuevo(),
                auditoriaVehiculo.getFechaAuditoria(),
                auditoriaVehiculo.getMotivo(),
                idAuditoria
        );
        return filasAfectadas > 0;
    }

    public boolean deleteById(Long idAuditoria) {
        String sql = "DELETE FROM auditorias_vehiculos WHERE id_auditoria = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idAuditoria);
        return filasAfectadas > 0;
    }
}
