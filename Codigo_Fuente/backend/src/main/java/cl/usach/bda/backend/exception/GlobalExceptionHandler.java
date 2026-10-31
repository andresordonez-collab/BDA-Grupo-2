package cl.usach.bda.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Recurso no encontrado (404 Not Found)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(NoSuchElementException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(RebalanceoNegocioException.class)
    public ResponseEntity<Map<String, Object>> manejarRebalanceo(RebalanceoNegocioException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, "REBALANCEO_RECHAZADO", ex.getMessage());
    }

    @ExceptionHandler(FinalizarViajeNegocioException.class)
    public ResponseEntity<Map<String, Object>> manejarFinalizarViaje(FinalizarViajeNegocioException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, "FINALIZAR_VIAJE_RECHAZADO", ex.getMessage());
    }

    // 2. Argumentos inválidos o reglas de negocio en Service (400 Bad Request)
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, Object>> manejarSolicitudInvalida(Exception ex) {
        String mensaje = ex instanceof IllegalArgumentException
                ? ex.getMessage()
                : "Datos de entrada inválidos o formato JSON incorrecto.";
        return construirRespuesta(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", mensaje);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> manejarTipoParametro(MethodArgumentTypeMismatchException ex,
                                                                   HttpServletRequest request) {
        if (esFinalizacionViaje(request)) {
            return construirRespuesta(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                    "Los identificadores deben ser enteros positivos dentro de su rango.");
        }
        return manejarErrorInterno(ex, request);
    }

    // 3. Violación de restricciones SQL: UNIQUE, FOREIGN KEY, CHECK, NOT NULL (409 Conflict / 400 Bad Request)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarIntegridadDatos(DataIntegrityViolationException ex, HttpServletRequest request) {
        if (esFinalizacionViaje(request)) {
            return manejarErrorInterno(ex, request);
        }
        String detalle = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        LOGGER.warn("Violación de integridad en BD: {}", detalle);
        return construirRespuesta(HttpStatus.CONFLICT, "VIOLACION_INTEGRIDAD_BD", detalle);
    }

    // 4. Errores generales de PostgreSQL, incluidos RAISE EXCEPTION de Triggers y SPs (SQLState P0001)
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> manejarErrorBaseDatos(DataAccessException ex, HttpServletRequest request) {
        // El servicio de rebalanceo ya clasifica los rechazos de negocio conocidos.
        if (esRebalanceo(request) || esFinalizacionViaje(request)) {
            return manejarErrorInterno(ex, request);
        }
        Throwable causaRaiz = ex.getMostSpecificCause();
        if (causaRaiz instanceof SQLException sqlEx && "P0001".equals(sqlEx.getSQLState())) {
            // Mensaje lanzado explícitamente por un Trigger o Procedimiento Almacenado
            return construirRespuesta(HttpStatus.BAD_REQUEST, "REGLA_NEGOCIO_BD", sqlEx.getMessage());
        }
        LOGGER.error("Error de acceso a datos en PostgreSQL", ex);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_BD", "Error al procesar la operación en la base de datos.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorInterno(Exception ex, HttpServletRequest request) {
        LOGGER.error("Error al procesar la solicitud", ex);
        String mensaje = esRebalanceo(request)
                ? "No fue posible completar el rebalanceo."
                : "No fue posible completar la solicitud.";
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", mensaje);
    }

    private boolean esFinalizacionViaje(HttpServletRequest request) {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        return ruta.matches("/api/viajes/[^/]+/finalizar");
    }

    private boolean esRebalanceo(HttpServletRequest request) {
        return "/api/rebalanceos".equals(request.getServletPath())
                || (request.getContextPath() + "/api/rebalanceos").equals(request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> construirRespuesta(HttpStatus estado, String codigo, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now());
        cuerpo.put("status", estado.value());
        cuerpo.put("codigo", codigo);
        cuerpo.put("mensaje", mensaje);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}