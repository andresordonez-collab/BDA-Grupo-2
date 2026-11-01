package cl.usach.bda.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;

import cl.usach.bda.backend.dto.ResumenMensualDTO;
import cl.usach.bda.backend.dto.ViajeHistorialDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ViajeRepository {
    private final JdbcTemplate jdbcTemplate;

    public ViajeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Antes del viaje, igual que PA1. No comprobar capacidad ni actividad aquí.
    public boolean bloquearEstacion(Long idEstacion) {
        return !jdbcTemplate.query("SELECT id_estacion FROM estaciones WHERE id_estacion = ? FOR UPDATE",
                (rs, row) -> rs.getLong("id_estacion"), idEstacion).isEmpty();
    }

    // Bloquear la fila completa impide cambiar id_usuario hasta finalizar la transacción.
    public Optional<Long> bloquearYObtenerPropietario(Long idViaje) {
        return jdbcTemplate.query("SELECT id_usuario FROM viajes WHERE id_viaje = ? FOR UPDATE",
                (rs, row) -> rs.getLong("id_usuario"), idViaje).stream().findFirst();
    }

    public void finalizar(Long idViaje, Long idEstacionDestino) {
        jdbcTemplate.execute("CALL finalizar_viaje(?, ?)", (PreparedStatementCallback<Void>) statement -> {
            statement.setLong(1, idViaje);
            statement.setLong(2, idEstacionDestino);
            statement.execute();
            return null;
        });
    }

    // El cálculo vive en la BD (fn_resumen_mensual_usuario); aquí solo se llama y se mapea.
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

    // Toda la lógica (bloqueos, tarifa, viaje, vehículo) vive en fn_iniciar_viaje.
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
