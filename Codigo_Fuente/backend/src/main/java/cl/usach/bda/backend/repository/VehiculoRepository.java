package cl.usach.bda.backend.repository;


import cl.usach.bda.backend.entities.VehiculoEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class VehiculoRepository {

    private final JdbcTemplate jdbcTemplate;

    public VehiculoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    // Row mapper para mapear las filas de la tabla a la entidad VehiculoEntity.

    private static final RowMapper<VehiculoEntity> VEHICULO_ROW_MAPPER =
            (rs, rowNum) -> VehiculoEntity.builder()
                    .idVehiculo(rs.getLong("id_vehiculo"))
                    .idTipoVehiculo(rs.getLong("id_tipo_vehiculo"))
                    .idEstacion(rs.getObject("id_estacion", Long.class))
                    .codigoVehiculo(rs.getString("codigo_vehiculo"))
                    .nivelBateria(rs.getObject("nivel_bateria", Integer.class))
                    .fechaAlta(rs.getObject("fecha_alta", LocalDateTime.class))
                    .estadoVehiculo(rs.getString("estado_vehiculo"))
                    .build();

    // CRUD.

    // CREATE

    // Crear un nuevo registro de vehículo.
    public VehiculoEntity save(VehiculoEntity vehiculo) {
        String sql = """
                INSERT INTO vehiculos (id_tipo_vehiculo, id_estacion, codigo_vehiculo, nivel_bateria, fecha_alta, estado_vehiculo)
                VALUES (?, ?, ?, ?, COALESCE(?, LOCALTIMESTAMP), ?)
                RETURNING id_vehiculo
                """;
        Long idGenerado = jdbcTemplate.queryForObject(sql, Long.class,
                vehiculo.getIdTipoVehiculo(),
                vehiculo.getIdEstacion(),
                vehiculo.getCodigoVehiculo(),
                vehiculo.getNivelBateria(),
                vehiculo.getFechaAlta(),
                vehiculo.getEstadoVehiculo()
        );
        vehiculo.setIdVehiculo(idGenerado);
        return findById(idGenerado)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo recuperar el vehículo recién creado."));
    }

    // Obtener todos los registros.
    public List<VehiculoEntity> findAll() {
        String sql = "SELECT * FROM vehiculos ORDER BY id_vehiculo ASC";
        return jdbcTemplate.query(sql, VEHICULO_ROW_MAPPER);
    }

    // Obtener un registro por su ID.
    public Optional<VehiculoEntity> findById(Long idVehiculo) {
        String sql = "SELECT * FROM vehiculos WHERE id_vehiculo = ?";
        return jdbcTemplate.query(sql, VEHICULO_ROW_MAPPER, idVehiculo)
                .stream()
                .findFirst();
    }

    // Actualizar un registro existente.
    public boolean update(Long idVehiculo, VehiculoEntity vehiculo, Long idOperador, String motivo) {
        // Debe ejecutarse dentro de la transacción del servicio, antes del UPDATE.
        if (idOperador != null) {
            jdbcTemplate.queryForObject("SELECT set_config('app.id_operador', ?, true)",
                    String.class, String.valueOf(idOperador));
        }
        if (idOperador != null || motivo != null) {
            jdbcTemplate.queryForObject("SELECT set_config('app.motivo', ?, true)",
                    String.class, motivo != null ? motivo : "");
        }
        String sql = "UPDATE vehiculos SET id_tipo_vehiculo = ?, id_estacion = ?, codigo_vehiculo = ?, nivel_bateria = ?, fecha_alta = COALESCE(?, fecha_alta), estado_vehiculo = ?"
                + " WHERE id_vehiculo = ?";
        int filasAfectadas = jdbcTemplate.update(sql,
                vehiculo.getIdTipoVehiculo(),
                vehiculo.getIdEstacion(),
                vehiculo.getCodigoVehiculo(),
                vehiculo.getNivelBateria(),
                vehiculo.getFechaAlta(),
                vehiculo.getEstadoVehiculo(),
                idVehiculo
        );
        return filasAfectadas > 0;
    }

    // Actualizar únicamente el estado y preparar los datos de auditoría.
    public boolean updateEstado(Long idVehiculo, String estadoVehiculo,
                                Long idOperador, String motivo) {

        jdbcTemplate.queryForObject(
                "SELECT set_config('app.id_operador', ?, true)",
                String.class,
                String.valueOf(idOperador)
        );

        jdbcTemplate.queryForObject(
                "SELECT set_config('app.motivo', ?, true)",
                String.class,
                motivo != null ? motivo : ""
        );

        String sql = """
            UPDATE vehiculos
            SET estado_vehiculo = ?
            WHERE id_vehiculo = ?
            """;

        int filasAfectadas = jdbcTemplate.update(
                sql,
                estadoVehiculo,
                idVehiculo
        );

        return filasAfectadas > 0;
    }

    // Eliminar un registro por su ID.
    public boolean deleteById(Long idVehiculo) {
        String sql = "DELETE FROM vehiculos WHERE id_vehiculo = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idVehiculo);
        return filasAfectadas > 0;
    }
}
