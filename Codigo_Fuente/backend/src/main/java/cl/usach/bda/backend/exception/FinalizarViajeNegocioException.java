package cl.usach.bda.backend.exception;

public class FinalizarViajeNegocioException extends RuntimeException {
    public FinalizarViajeNegocioException() {
        super("El viaje no puede finalizarse en la estación indicada.");
    }

    public FinalizarViajeNegocioException(Throwable cause) {
        super("El viaje no puede finalizarse en la estación indicada.", cause);
    }
}
