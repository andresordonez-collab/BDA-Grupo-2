package cl.usach.bda.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tools.jackson.databind.annotation.JsonDeserialize;

public record FinalizarViajeRequestDTO(
        @NotNull @Positive
        @JsonDeserialize(using = RebalanceoRequestDTO.EnteroLargo.class) Long idEstacionDestino
) {
}
