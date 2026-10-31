package cl.usach.bda.backend.controller;

import cl.usach.bda.backend.dto.RebalanceoErrorDTO;
import cl.usach.bda.backend.exception.RebalanceoNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes = RebalanceoController.class)
public class RebalanceoExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RebalanceoExceptionHandler.class);

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RebalanceoErrorDTO solicitudInvalida(Exception exception) {
        return new RebalanceoErrorDTO("SOLICITUD_INVALIDA", "Debe enviar los cuatro campos requeridos con valores enteros dentro de su rango.");
    }

    @ExceptionHandler(RebalanceoNegocioException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RebalanceoErrorDTO rechazo(RebalanceoNegocioException exception) {
        return new RebalanceoErrorDTO("REBALANCEO_RECHAZADO", exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RebalanceoErrorDTO errorInterno(Exception exception) {
        LOGGER.error("Error al ejecutar rebalancear_flota", exception);
        return new RebalanceoErrorDTO("ERROR_INTERNO", "No fue posible completar el rebalanceo.");
    }
}
