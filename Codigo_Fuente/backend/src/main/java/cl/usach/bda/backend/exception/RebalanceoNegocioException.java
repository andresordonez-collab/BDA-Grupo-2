package cl.usach.bda.backend.exception;

public class RebalanceoNegocioException extends RuntimeException {
    public RebalanceoNegocioException(Throwable cause) {
        super("El rebalanceo no cumple las condiciones de negocio de estaciones, operador, cantidad, disponibilidad o capacidad.", cause);
    }
}
