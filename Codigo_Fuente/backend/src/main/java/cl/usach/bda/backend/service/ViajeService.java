package cl.usach.bda.backend.service;

import cl.usach.bda.backend.dto.ResumenMensualDTO;
import cl.usach.bda.backend.exception.FinalizarViajeNegocioException;
import cl.usach.bda.backend.repository.ViajeRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ViajeService {
    private static final List<String> RECHAZOS = List.of(
            "Error: La estación de destino ", "Error: El viaje ");
    private final ViajeRepository repository;

    public ViajeService(ViajeRepository repository) {
        this.repository = repository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void finalizar(Long idViaje, Long idEstacionDestino, Long idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new AuthenticationCredentialsNotFoundException("Se requiere un usuario autenticado.");
        }
        if (idViaje == null || idViaje <= 0 || idEstacionDestino == null || idEstacionDestino <= 0) {
            throw new IllegalArgumentException("Los identificadores deben ser enteros positivos.");
        }
        try {
            // No invertir el orden de PA1: estación -> viaje -> CALL.
            // Si la estación no existe, no tomar el bloqueo del viaje.
            if (!repository.bloquearEstacion(idEstacionDestino)) {
                throw new FinalizarViajeNegocioException();
            }
            Long propietario = repository.bloquearYObtenerPropietario(idViaje)
                    .orElseThrow(ViajeService::viajeNoEncontrado);
            if (!idUsuario.equals(propietario)) {
                throw viajeNoEncontrado();
            }
            repository.finalizar(idViaje, idEstacionDestino);
        } catch (DataAccessException exception) {
            for (Throwable cause = exception.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof SQLException sql && "P0001".equals(sql.getSQLState())
                        && sql.getMessage() != null
                        && RECHAZOS.stream().anyMatch(prefix -> sql.getMessage().startsWith(prefix)
                                || sql.getMessage().startsWith("ERROR: " + prefix))) {
                    throw new FinalizarViajeNegocioException(exception);
                }
            }
            throw exception;
        }
    }

    private static NoSuchElementException viajeNoEncontrado() {
        return new NoSuchElementException("Viaje no encontrado.");
    }

    // Sin anio/mes se usa el mes actual. Se valida antes de llegar a la BD: make_date falla con mes 13.
    public ResumenMensualDTO resumenMensual(Long idUsuario, Integer anio, Integer mes) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("El identificador de usuario debe ser un entero positivo.");
        }
        YearMonth actual = YearMonth.now();
        int anioConsulta = anio != null ? anio : actual.getYear();
        int mesConsulta = mes != null ? mes : actual.getMonthValue();
        if (mesConsulta < 1 || mesConsulta > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }
        if (anioConsulta < 2000 || anioConsulta > 2100) {
            throw new IllegalArgumentException("El año debe estar entre 2000 y 2100.");
        }
        return repository.obtenerResumenMensual(idUsuario, anioConsulta, mesConsulta);
    }
}
