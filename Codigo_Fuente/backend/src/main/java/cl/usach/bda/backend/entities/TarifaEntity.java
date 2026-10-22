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
public class TarifaEntity {

    private Long idTarifa;
    private Long idTipoVehiculo;
    private BigDecimal valorDesbloqueo;
    private BigDecimal valorMinuto;
    private LocalDateTime vigenteDesde;
    private Boolean activa;
}
