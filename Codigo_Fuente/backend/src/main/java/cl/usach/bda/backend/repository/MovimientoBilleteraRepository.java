package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.entities.MovimientoBilleteraEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MovimientoBilleteraRepository {

    private final JdbcTemplate jdbcTemplate;

    public MovimientoBilleteraRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Row mapper para mapear las filas de la tabla a la entidad.
    private static final RowMapper<MovimientoBilleteraEntity> MOVIMIENTOS_BILLETERAS_ROW_MAPPER = (rs, rowNum) ->
            MovimientoBilleteraEntity.builder()
                    .idMovimiento(rs.getLong("id_movimiento"))
                    .idBilletera(rs.getLong("id_billetera"))
                    .idViaje(rs.getObject("id_viaje", Long.class))
                    .tipoMovimiento(rs.getString("tipo_movimiento"))
                    .monto(rs.getBigDecimal("monto"))
                    .fechaMovimiento(rs.getObject("fecha_movimiento", LocalDateTime.class))
                    .build();

    // Crear un registro y obtener el ID generado por PostgreSQL.
    public MovimientoBilleteraEntity save(MovimientoBilleteraEntity movimientoBilletera) {
        String sql = "INSERT INTO movimientos_billeteras (id_billetera, id_viaje, tipo_movimiento, monto, fecha_movimiento)"
                + " VALUES (?, ?, ?, ?, COALESCE(?, LOCALTIMESTAMP)) RETURNING id_movimiento";
        Long idGenerado = jdbcTemplate.queryForObject(sql, Long.class,
                movimientoBilletera.getIdBilletera(),
                movimientoBilletera.getIdViaje(),
                movimientoBilletera.getTipoMovimiento(),
                movimientoBilletera.getMonto(),
                movimientoBilletera.getFechaMovimiento()
        );
        movimientoBilletera.setIdMovimiento(idGenerado);
        // Leer los valores predeterminados aplicados por PostgreSQL.
        return findById(idGenerado).orElseThrow();
    }

    // Obtener todos los registros.
    public List<MovimientoBilleteraEntity> findAll() {
        String sql = "SELECT * FROM movimientos_billeteras ORDER BY id_movimiento ASC";
        return jdbcTemplate.query(sql, MOVIMIENTOS_BILLETERAS_ROW_MAPPER);
    }

    // Obtener un registro por su ID.
    public Optional<MovimientoBilleteraEntity> findById(Long idMovimiento) {
        String sql = "SELECT * FROM movimientos_billeteras WHERE id_movimiento = ?";
        return jdbcTemplate.query(sql, MOVIMIENTOS_BILLETERAS_ROW_MAPPER, idMovimiento)
                .stream()
                .findFirst();
    }

    // Actualizar un registro existente.
    public boolean update(Long idMovimiento, MovimientoBilleteraEntity movimientoBilletera) {
        String sql = "UPDATE movimientos_billeteras SET id_billetera = ?, id_viaje = ?, tipo_movimiento = ?, monto = ?, fecha_movimiento = COALESCE(?, fecha_movimiento)"
                + " WHERE id_movimiento = ?";
        int filasAfectadas = jdbcTemplate.update(sql,
                movimientoBilletera.getIdBilletera(),
                movimientoBilletera.getIdViaje(),
                movimientoBilletera.getTipoMovimiento(),
                movimientoBilletera.getMonto(),
                movimientoBilletera.getFechaMovimiento(),
                idMovimiento
        );
        return filasAfectadas > 0;
    }

    // Eliminar un registro por su ID.
    public boolean deleteById(Long idMovimiento) {
        String sql = "DELETE FROM movimientos_billeteras WHERE id_movimiento = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idMovimiento);
        return filasAfectadas > 0;
    }
}
