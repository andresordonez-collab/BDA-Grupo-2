package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.dto.RebalanceoRequestDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;

import java.sql.Types;

@Repository
public class RebalanceoRepository {
    private final JdbcTemplate jdbcTemplate;

    public RebalanceoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void rebalancear(RebalanceoRequestDTO solicitud, Long idOperador) {
        jdbcTemplate.execute("CALL rebalancear_flota(?, ?, ?, ?, ?)",
                (PreparedStatementCallback<Void>) statement -> {
                    statement.setLong(1, solicitud.idEstacionOrigen());
                    statement.setLong(2, solicitud.idEstacionDestino());
                    statement.setLong(3, idOperador);
                    statement.setInt(4, solicitud.cantidadVehiculos());
                    // Tipo opcional: NULL significa "cualquier tipo de vehículo".
                    if (solicitud.idTipoVehiculo() == null) {
                        statement.setNull(5, Types.BIGINT);
                    } else {
                        statement.setLong(5, solicitud.idTipoVehiculo());
                    }
                    statement.execute();
                    return null;
                });
    }
}
