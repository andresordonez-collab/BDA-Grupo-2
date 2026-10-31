package cl.usach.bda.backend.service;

import cl.usach.bda.backend.dto.RebalanceoRequestDTO;
import cl.usach.bda.backend.exception.RebalanceoNegocioException;
import cl.usach.bda.backend.repository.RebalanceoRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class RebalanceoService {
    // RAISE EXCEPTION sin ERRCODE usa P0001. Reconocer solo rechazos conocidos
    // evita presentar errores de configuración (p. ej. aislamiento) como negocio.
    private static final List<String> RECHAZOS = List.of(
            "La cantidad de vehículos debe ser mayor que cero.",
            "Debe indicar las estaciones de origen y destino.",
            "Las estaciones de origen y destino deben ser diferentes.",
            "La estación de origen ", "La estación de destino ", "El usuario ");

    private final RebalanceoRepository repository;

    public RebalanceoService(RebalanceoRepository repository) {
        this.repository = repository;
    }

    public void rebalancear(RebalanceoRequestDTO solicitud, Long idOperador) {
        try {
            repository.rebalancear(solicitud, idOperador);
        } catch (DataAccessException exception) {
            for (Throwable cause = exception.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof SQLException sql && "P0001".equals(sql.getSQLState())
                        && sql.getMessage() != null
                        && RECHAZOS.stream().anyMatch(texto -> sql.getMessage().startsWith(texto)
                                || sql.getMessage().startsWith("ERROR: " + texto))) {
                    throw new RebalanceoNegocioException(exception);
                }
            }
            throw exception;
        }
    }
}
