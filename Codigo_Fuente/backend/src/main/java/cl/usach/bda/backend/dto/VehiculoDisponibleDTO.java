package cl.usach.bda.backend.dto;

public record VehiculoDisponibleDTO(
        Long idVehiculo,
        String codigoVehiculo,
        String tipoVehiculo,
        Integer nivelBateria
) {
}
