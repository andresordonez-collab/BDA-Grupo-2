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

    // Row mapper para mapear las filas de la tabla a la entidad.
    private static final RowMapper<BilleteraEntity> BILLETERAS_ROW_MAPPER = (rs, rowNum) ->
            BilleteraEntity.builder()
                    .idBilletera(rs.getLong("id_billetera"))
                    .idUsuario(rs.getLong("id_usuario"))
                    .saldo(rs.getBigDecimal("saldo"))
                    .build();

    // Crear un registro y obtener el ID generado por PostgreSQL.
    public BilleteraEntity save(BilleteraEntity billetera) {
        String sql = "INSERT INTO billeteras (id_usuario, saldo)"
                + " VALUES (?, COALESCE(?, 0.00)) RETURNING id_billetera";
        Long idGenerado = jdbcTemplate.queryForObject(sql, Long.class,
                billetera.getIdUsuario(),
                billetera.getSaldo()
        );
        billetera.setIdBilletera(idGenerado);
        // Leer los valores predeterminados aplicados por PostgreSQL.
        return findById(idGenerado).orElseThrow();
    }

    // Obtener todos los registros.
    public List<BilleteraEntity> findAll() {
        String sql = "SELECT * FROM billeteras ORDER BY id_billetera ASC";
        return jdbcTemplate.query(sql, BILLETERAS_ROW_MAPPER);
    }

    // Obtener un registro por su ID.
    public Optional<BilleteraEntity> findById(Long idBilletera) {
        String sql = "SELECT * FROM billeteras WHERE id_billetera = ?";
        return jdbcTemplate.query(sql, BILLETERAS_ROW_MAPPER, idBilletera)
                .stream()
                .findFirst();
    }

    // Actualizar un registro existente.
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

    // Eliminar un registro por su ID.
    public boolean deleteById(Long idBilletera) {
        String sql = "DELETE FROM billeteras WHERE id_billetera = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idBilletera);
        return filasAfectadas > 0;
    }

    // Billetera de un usuario (una por usuario).
    public Optional<BilleteraEntity> findByIdUsuario(Long idUsuario) {
        String sql = "SELECT * FROM billeteras WHERE id_usuario = ?";
        return jdbcTemplate.query(sql, BILLETERAS_ROW_MAPPER, idUsuario)
                .stream()
                .findFirst();
    }

    // Últimos movimientos de una billetera, del más reciente al más antiguo.
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
