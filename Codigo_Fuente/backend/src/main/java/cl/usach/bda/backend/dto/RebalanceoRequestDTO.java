package cl.usach.bda.backend.dto;

import jakarta.validation.constraints.NotNull;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

/** Solo valida presencia; las reglas de negocio pertenecen a rebalancear_flota. */
public record RebalanceoRequestDTO(
        @NotNull @JsonDeserialize(using = EnteroLargo.class) Long idEstacionOrigen,
        @NotNull @JsonDeserialize(using = EnteroLargo.class) Long idEstacionDestino,
        @NotNull @JsonDeserialize(using = Entero.class) Integer cantidadVehiculos
) {
    // Rechazar coerciones de texto/decimales solo para este contrato, sin cambiar
    // el ObjectMapper ni las solicitudes de otros controladores.
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
