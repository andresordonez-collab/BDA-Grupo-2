package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.dto.RebalanceoRequestDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;

@Repository
public class RebalanceoRepository {
    private final JdbcTemplate jdbcTemplate;

    public RebalanceoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Tarea 4: Llama al procedimiento rebalancear_flota.
    public void rebalancear(RebalanceoRequestDTO solicitud, Long idOperador) {
        jdbcTemplate.execute("CALL rebalancear_flota(?, ?, ?, ?, ?)",
                (PreparedStatementCallback<Void>) statement -> {
                    statement.setLong(1, solicitud.idEstacionOrigen());
                    statement.setLong(2, solicitud.idEstacionDestino());
                    statement.setLong(3, idOperador);
                    statement.setInt(4, solicitud.cantidadVehiculos());
                    statement.setLong(5, solicitud.idTipoVehiculo());
                    statement.execute();
                    return null;
                });
    }
}
