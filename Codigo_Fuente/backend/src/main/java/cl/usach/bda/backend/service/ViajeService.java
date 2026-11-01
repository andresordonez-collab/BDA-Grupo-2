package cl.usach.bda.backend.service;

import cl.usach.bda.backend.dto.ResumenMensualDTO;
import cl.usach.bda.backend.dto.ViajeHistorialDTO;
import cl.usach.bda.backend.entities.ViajeEntity;
import cl.usach.bda.backend.exception.FinalizarViajeNegocioException;
import cl.usach.bda.backend.exception.IniciarViajeNegocioException;
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

    // Rechazos de fn_iniciar_viaje y de trigger_validar_inicio_viaje que se muestran al cliente.
    private static final List<String> RECHAZOS_INICIO = List.of(
            "No puede iniciar el viaje", "Nivel de batería insuficiente",
            "El usuario ya tiene un viaje en curso", "Error: El usuario ",
            "Error: El vehículo ", "Error: No hay una tarifa activa");

    private static NoSuchElementException viajeNoEncontrado() {
        return new NoSuchElementException("Viaje no encontrado.");
    }

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

    @Transactional
    public ViajeEntity guardarViaje(ViajeEntity viaje) {
        return repository.save(viaje);
    }

    public List<ViajeEntity> listarViajes() {
        return repository.findAll();
    }

    public ViajeEntity obtenerViajePorId(Long idViaje) {
        return repository.findById(idViaje)
                .orElseThrow(() -> new NoSuchElementException("Viaje con ID " + idViaje + " no encontrado."));
    }

    @Transactional
    public ViajeEntity actualizarViaje(Long idViaje, ViajeEntity viaje) {
        boolean actualizado = repository.update(idViaje, viaje);
        if (!actualizado) {
            throw new NoSuchElementException("Viaje con ID " + idViaje + " no encontrado para actualizar.");
        }
        return obtenerViajePorId(idViaje);
    }

    @Transactional
    public void eliminarViaje(Long idViaje) {
        boolean eliminado = repository.deleteById(idViaje);
        if (!eliminado) {
            throw new NoSuchElementException("Viaje con ID " + idViaje + " no encontrado para eliminar.");
        }
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

    // Inicia un viaje: valida al usuario y al vehículo, crea el viaje con la tarifa vigente
    // y saca el vehículo de la estación, todo en una sola transacción.
    public Long iniciar(Long idVehiculo, Long idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new AuthenticationCredentialsNotFoundException("Se requiere un usuario autenticado.");
        }
        if (idVehiculo == null || idVehiculo <= 0) {
            throw new IllegalArgumentException("El identificador del vehículo debe ser un entero positivo.");
        }
        try {
            return repository.iniciar(idUsuario, idVehiculo);
        } catch (DataAccessException exception) {
            for (Throwable cause = exception.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof SQLException sql && "P0001".equals(sql.getSQLState())
                        && sql.getMessage() != null) {
                    String mensaje = mensajePrincipal(sql.getMessage());
                    if (RECHAZOS_INICIO.stream().anyMatch(mensaje::startsWith)) {
                        throw new IniciarViajeNegocioException(
                                mensaje.startsWith("Error: ") ? mensaje.substring("Error: ".length()) : mensaje,
                                exception);
                    }
                }
            }
            throw exception;
        }
    }

    // El mensaje del driver trae "ERROR: " y líneas de contexto (Where:); solo sirve la primera.
    private static String mensajePrincipal(String mensajeSql) {
        String primera = mensajeSql.lines().findFirst().orElse("");
        return primera.startsWith("ERROR: ") ? primera.substring("ERROR: ".length()) : primera;
    }

    public List<ViajeHistorialDTO> historial(Long idUsuario, Integer limite) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("El identificador de usuario debe ser un entero positivo.");
        }
        int tope = limite != null ? limite : 50;
        if (tope < 1 || tope > 200) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 200.");
        }
        return repository.listarPorUsuario(idUsuario, tope);
    }
}
