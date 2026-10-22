package cl.usach.bda.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleRebalanceoEntity {

    private Long idOrden;
    private Long idVehiculo;
}
