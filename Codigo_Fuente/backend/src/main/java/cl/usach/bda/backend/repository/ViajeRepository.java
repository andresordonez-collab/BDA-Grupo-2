package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.entities.ViajeEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import cl.usach.bda.backend.dto.ResumenMensualDTO;
import cl.usach.bda.backend.dto.ViajeHistorialDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ViajeRepository {
    private final JdbcTemplate jdbcTemplate;

    public ViajeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ViajeEntity> VIAJES_ROW_MAPPER = (rs, rowNum) ->
            ViajeEntity.builder()
                    .idViaje(rs.getLong("id_viaje"))
                    .idUsuario(rs.getLong("id_usuario"))
                    .idVehiculo(rs.getLong("id_vehiculo"))
                    .idOrigen(rs.getLong("id_origen"))
                    .idDestino(rs.getObject("id_destino", Long.class))
                    .idTarifa(rs.getLong("id_tarifa"))
                    .fechaInicio(rs.getObject("fecha_inicio", LocalDateTime.class))
                    .fechaFin(rs.getObject("fecha_fin", LocalDateTime.class))
                    .minutosViaje(rs.getObject("minutos_viaje", Integer.class))
                    .costo(rs.getObject("costo", BigDecimal.class))
                    .estadoViaje(rs.getString("estado_viaje"))
                    .build();

    public ViajeEntity save(ViajeEntity viaje) {
        String sql = "INSERT INTO viajes (id_usuario, id_vehiculo, id_origen, id_destino, id_tarifa, fecha_inicio, fecha_fin, minutos_viaje, costo, estado_viaje)"
                + " VALUES (?, ?, ?, ?, ?, COALESCE(?, LOCALTIMESTAMP), ?, ?, ?, ?) RETURNING id_viaje";
        Long idGenerado = jdbcTemplate.queryForObject(sql, Long.class,
                viaje.getIdUsuario(),
                viaje.getIdVehiculo(),
                viaje.getIdOrigen(),
                viaje.getIdDestino(),
                viaje.getIdTarifa(),
                viaje.getFechaInicio(),
                viaje.getFechaFin(),
                viaje.getMinutosViaje(),
                viaje.getCosto(),
                viaje.getEstadoViaje()
        );
        viaje.setIdViaje(idGenerado);
        return findById(idGenerado).orElseThrow();
    }

    public List<ViajeEntity> findAll() {
        String sql = "SELECT * FROM viajes ORDER BY id_viaje ASC";
        return jdbcTemplate.query(sql, VIAJES_ROW_MAPPER);
    }

    public Optional<ViajeEntity> findById(Long idViaje) {
        String sql = "SELECT * FROM viajes WHERE id_viaje = ?";
        return jdbcTemplate.query(sql, VIAJES_ROW_MAPPER, idViaje)
                .stream()
                .findFirst();
    }

    public boolean update(Long idViaje, ViajeEntity viaje) {
        String sql = "UPDATE viajes SET id_usuario = ?, id_vehiculo = ?, id_origen = ?, id_destino = ?, id_tarifa = ?, fecha_inicio = COALESCE(?, fecha_inicio), fecha_fin = ?, minutos_viaje = ?, costo = ?, estado_viaje = ?"
                + " WHERE id_viaje = ?";
        int filasAfectadas = jdbcTemplate.update(sql,
                viaje.getIdUsuario(),
                viaje.getIdVehiculo(),
                viaje.getIdOrigen(),
                viaje.getIdDestino(),
                viaje.getIdTarifa(),
                viaje.getFechaInicio(),
                viaje.getFechaFin(),
                viaje.getMinutosViaje(),
                viaje.getCosto(),
                viaje.getEstadoViaje(),
                idViaje
        );
        return filasAfectadas > 0;
    }

    public boolean deleteById(Long idViaje) {
        String sql = "DELETE FROM viajes WHERE id_viaje = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idViaje);
        return filasAfectadas > 0;
    }

    public boolean bloquearEstacion(Long idEstacion) {
        return !jdbcTemplate.query("SELECT id_estacion FROM estaciones WHERE id_estacion = ? FOR UPDATE",
                (rs, row) -> rs.getLong("id_estacion"), idEstacion).isEmpty();
    }

    public Optional<Long> bloquearYObtenerPropietario(Long idViaje) {
        return jdbcTemplate.query("SELECT id_usuario FROM viajes WHERE id_viaje = ? FOR UPDATE",
                (rs, row) -> rs.getLong("id_usuario"), idViaje).stream().findFirst();
    }

    // Tarea 3: Llama al procedimiento finalizar_viaje.
    public void finalizar(Long idViaje, Long idEstacionDestino) {
        jdbcTemplate.execute("CALL finalizar_viaje(?, ?)", (PreparedStatementCallback<Void>) statement -> {
            statement.setLong(1, idViaje);
            statement.setLong(2, idEstacionDestino);
            statement.execute();
            return null;
        });
    }

    // Tarea 10: Llama a fn_resumen_mensual_usuario, que calcula viajes, minutos y gasto.
    public ResumenMensualDTO obtenerResumenMensual(Long idUsuario, int anio, int mes) {
        return jdbcTemplate.queryForObject(
                "SELECT cantidad_viajes, minutos_totales, gasto_total FROM fn_resumen_mensual_usuario(?, ?, ?)",
                (rs, row) -> new ResumenMensualDTO(
                        anio,
                        mes,
                        rs.getLong("cantidad_viajes"),
                        rs.getLong("minutos_totales"),
                        rs.getBigDecimal("gasto_total")),
                idUsuario, anio, mes);
    }

    // Tarea 2: Llama a fn_iniciar_viaje.
    public Long iniciar(Long idUsuario, Long idVehiculo) {
        return jdbcTemplate.queryForObject("SELECT fn_iniciar_viaje(?, ?)", Long.class, idUsuario, idVehiculo);
    }

    public List<ViajeHistorialDTO> listarPorUsuario(Long idUsuario, int limite) {
        String sql = """
                SELECT
                    v.id_viaje,
                    ve.codigo_vehiculo,
                    tv.nombre_tipo_vehiculo,
                    eo.nombre_estacion AS estacion_origen,
                    ed.nombre_estacion AS estacion_destino,
                    v.fecha_inicio,
                    v.fecha_fin,
                    v.minutos_viaje,
                    v.costo,
                    v.estado_viaje
                FROM viajes v
                JOIN vehiculos ve ON ve.id_vehiculo = v.id_vehiculo
                JOIN tipos_vehiculos tv ON tv.id_tipo_vehiculo = ve.id_tipo_vehiculo
                JOIN estaciones eo ON eo.id_estacion = v.id_origen
                LEFT JOIN estaciones ed ON ed.id_estacion = v.id_destino
                WHERE v.id_usuario = ?
                ORDER BY v.fecha_inicio DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, row) -> new ViajeHistorialDTO(
                rs.getLong("id_viaje"),
                rs.getString("codigo_vehiculo"),
                rs.getString("nombre_tipo_vehiculo"),
                rs.getString("estacion_origen"),
                rs.getString("estacion_destino"),
                rs.getObject("fecha_inicio", LocalDateTime.class),
                rs.getObject("fecha_fin", LocalDateTime.class),
                rs.getObject("minutos_viaje", Integer.class),
                rs.getBigDecimal("costo"),
                rs.getString("estado_viaje")
        ), idUsuario, limite);
    }
}
