package cl.usach.bda.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;

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
}
