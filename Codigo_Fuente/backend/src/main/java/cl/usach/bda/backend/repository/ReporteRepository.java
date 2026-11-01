package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.dto.DemandaEstacionHorariaDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReporteRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReporteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<DemandaEstacionHorariaDTO> DEMANDA_ESTACION_HORARIA_ROW_MAPPER = (rs, rowNum) ->
            DemandaEstacionHorariaDTO.builder()
                    .idEstacion(rs.getLong("id_estacion"))
                    .nombreEstacion(rs.getString("nombre_estacion"))
                    .franjaHoraria(rs.getInt("franja_horaria"))
                    .viajesIniciados(rs.getLong("viajes_iniciados"))
                    .viajesFinalizados(rs.getLong("viajes_finalizados"))
                    .build();

    public List<DemandaEstacionHorariaDTO> obtenerDemandaEstacionHoraria() {
        String sql = """
                SELECT id_estacion, nombre_estacion, franja_horaria,
                       viajes_iniciados, viajes_finalizados
                FROM mv_demanda_estacion_horaria
                ORDER BY id_estacion ASC, franja_horaria ASC
                """;
        return jdbcTemplate.query(sql, DEMANDA_ESTACION_HORARIA_ROW_MAPPER);
    }
}
