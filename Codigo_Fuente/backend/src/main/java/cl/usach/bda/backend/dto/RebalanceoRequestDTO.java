package cl.usach.bda.backend.dto;

import jakarta.validation.constraints.NotNull;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

public record RebalanceoRequestDTO(
        @NotNull @JsonDeserialize(using = EnteroLargo.class) Long idEstacionOrigen,
        @NotNull @JsonDeserialize(using = EnteroLargo.class) Long idEstacionDestino,
        @NotNull @JsonDeserialize(using = Entero.class) Integer cantidadVehiculos,
        @NotNull @JsonDeserialize(using = EnteroLargo.class) Long idTipoVehiculo
) {
    public static class EnteroLargo extends ValueDeserializer<Long> {
        @Override
        public Long deserialize(JsonParser parser, DeserializationContext context) {
            if (!parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                return context.reportInputMismatch(Long.class, "Se requiere un entero BIGINT.");
            }
            return parser.getLongValue();
        }
    }

    public static class Entero extends ValueDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonParser parser, DeserializationContext context) {
            if (!parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                return context.reportInputMismatch(Integer.class, "Se requiere un entero INT.");
            }
            return parser.getIntValue();
        }
    }
}
