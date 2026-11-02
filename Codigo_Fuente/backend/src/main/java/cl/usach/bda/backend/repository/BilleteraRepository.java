package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.dto.MovimientoBilleteraResumenDTO;
import cl.usach.bda.backend.entities.BilleteraEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class BilleteraRepository {

    private final JdbcTemplate jdbcTemplate;

    public BilleteraRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<BilleteraEntity> BILLETERAS_ROW_MAPPER = (rs, rowNum) ->
            BilleteraEntity.builder()
                    .idBilletera(rs.getLong("id_billetera"))
                    .idUsuario(rs.getLong("id_usuario"))
                    .saldo(rs.getBigDecimal("saldo"))
                    .build();

    public BilleteraEntity save(BilleteraEntity billetera) {
        String sql = "INSERT INTO billeteras (id_usuario, saldo)"
                + " VALUES (?, COALESCE(?, 0.00)) RETURNING id_billetera";
        Long idGenerado = jdbcTemplate.queryForObject(sql, Long.class,
                billetera.getIdUsuario(),
                billetera.getSaldo()
        );
        billetera.setIdBilletera(idGenerado);
        return findById(idGenerado).orElseThrow();
    }

    public List<BilleteraEntity> findAll() {
        String sql = "SELECT * FROM billeteras ORDER BY id_billetera ASC";
        return jdbcTemplate.query(sql, BILLETERAS_ROW_MAPPER);
    }

    public Optional<BilleteraEntity> findById(Long idBilletera) {
        String sql = "SELECT * FROM billeteras WHERE id_billetera = ?";
        return jdbcTemplate.query(sql, BILLETERAS_ROW_MAPPER, idBilletera)
                .stream()
                .findFirst();
    }

    public boolean update(Long idBilletera, BilleteraEntity billetera) {
        String sql = "UPDATE billeteras SET id_usuario = ?, saldo = COALESCE(?, saldo)"
                + " WHERE id_billetera = ?";
        int filasAfectadas = jdbcTemplate.update(sql,
                billetera.getIdUsuario(),
                billetera.getSaldo(),
                idBilletera
        );
        return filasAfectadas > 0;
    }

    public boolean deleteById(Long idBilletera) {
        String sql = "DELETE FROM billeteras WHERE id_billetera = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idBilletera);
        return filasAfectadas > 0;
    }

    public Optional<BilleteraEntity> findByIdUsuario(Long idUsuario) {
        String sql = "SELECT * FROM billeteras WHERE id_usuario = ?";
        return jdbcTemplate.query(sql, BILLETERAS_ROW_MAPPER, idUsuario)
                .stream()
                .findFirst();
    }

    public boolean duenoTieneViajeActivo(Long idBilletera) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM viajes v
                    JOIN billeteras b ON b.id_usuario = v.id_usuario
                    WHERE b.id_billetera = ?
                      AND v.estado_viaje = 'Activo'
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, idBilletera));
    }

    public List<MovimientoBilleteraResumenDTO> listarMovimientosRecientes(Long idBilletera, int limite) {
        String sql = """
                SELECT id_movimiento, tipo_movimiento, monto, fecha_movimiento, id_viaje
                FROM movimientos_billeteras
                WHERE id_billetera = ?
                ORDER BY fecha_movimiento DESC, id_movimiento DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new MovimientoBilleteraResumenDTO(
                rs.getLong("id_movimiento"),
                rs.getString("tipo_movimiento"),
                rs.getBigDecimal("monto"),
                rs.getObject("fecha_movimiento", LocalDateTime.class),
                rs.getObject("id_viaje", Long.class)
        ), idBilletera, limite);
    }
}
