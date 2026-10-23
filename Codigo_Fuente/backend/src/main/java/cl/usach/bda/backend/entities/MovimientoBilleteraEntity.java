package cl.usach.bda.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoBilleteraEntity {

    private Long idMovimiento;
    private Long idBilletera;
    private Long idViaje;
    private String tipoMovimiento;
    private BigDecimal monto;
    private LocalDateTime fechaMovimiento;
}
