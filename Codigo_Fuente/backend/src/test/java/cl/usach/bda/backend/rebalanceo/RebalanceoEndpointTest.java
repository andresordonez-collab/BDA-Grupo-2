package cl.usach.bda.backend.rebalanceo;

import cl.usach.bda.backend.controller.RebalanceoController;
import cl.usach.bda.backend.exception.GlobalExceptionHandler;
import cl.usach.bda.backend.dto.RebalanceoRequestDTO;
import cl.usach.bda.backend.repository.RebalanceoRepository;
import cl.usach.bda.backend.service.RebalanceoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RebalanceoEndpointTest {
    private RebalanceoRepository repository;
    private MockMvc mvc;
    private static final String BODY = """
            {"idEstacionOrigen":1,"idEstacionDestino":2,"idOperador":3,"cantidadVehiculos":4}
            """;

    @BeforeEach
    void setup() {
        repository = mock(RebalanceoRepository.class);
        mvc = MockMvcBuilders.standaloneSetup(new RebalanceoController(new RebalanceoService(repository)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void completaSinContenido() throws Exception {
        mvc.perform(post("/api/rebalanceos").contentType("application/json").content(BODY))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(repository).rebalancear(new RebalanceoRequestDTO(1L, 2L, 3L, 4));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{", "{}", "null",
            "{\"idEstacionOrigen\":\"abc\",\"idEstacionDestino\":2,\"idOperador\":3,\"cantidadVehiculos\":4}",
            "{\"idEstacionOrigen\":1,\"idEstacionDestino\":2,\"idOperador\":3,\"cantidadVehiculos\":2147483648}",
            "{\"idEstacionOrigen\":1,\"idEstacionDestino\":2,\"idOperador\":null,\"cantidadVehiculos\":4}"})
    void rechazaJsonInvalidoSinInvocarBase(String body) throws Exception {
        mvc.perform(post("/api/rebalanceos").contentType("application/json").content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "La cantidad de vehículos debe ser mayor que cero.",
            "Debe indicar las estaciones de origen y destino.",
            "Las estaciones de origen y destino deben ser diferentes.",
            "La estación de origen 1 no existe o está inactiva.",
            "La estación de destino 2 no existe o está inactiva.",
            "El usuario 3 no existe, está inactivo o no tiene el rol Operador.",
            "La estación de origen 1 tiene solo 0 vehículos disponibles; se solicitaron 4.",
            "La estación de destino 2 no tiene capacidad para recibir 4 vehículos (ocupación: 10, capacidad: 10)."})
    void traduceRechazosDelProcedimiento(String mensaje) throws Exception {
        doThrow(new UncategorizedSQLException("CALL", "CALL", new SQLException("ERROR: " + mensaje, "P0001")))
                .when(repository).rebalancear(any());
        mvc.perform(post("/api/rebalanceos").contentType("application/json").content(BODY))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("REBALANCEO_RECHAZADO"));
    }

    @Test
    void rechazaDecimalesYCoercionesDeTextoEnCadaCampo() throws Exception {
        for (String campo : new String[]{"idEstacionOrigen", "idEstacionDestino", "idOperador", "cantidadVehiculos"}) {
            for (String valor : new String[]{"1.5", "\"1\"", "true", "9223372036854775808"}) {
                String body = BODY.replaceFirst("\"" + campo + "\":\\d+", "\"" + campo + "\":" + valor);
                mvc.perform(post("/api/rebalanceos").contentType("application/json").content(body))
                        .andExpect(status().isBadRequest());
            }
        }
        verifyNoInteractions(repository);
    }

    @Test
    void delegaCantidadInvalidaYEstacionesIgualesAlProcedimiento() throws Exception {
        mvc.perform(post("/api/rebalanceos").contentType("application/json")
                .content(BODY.replace("\"idEstacionDestino\":2", "\"idEstacionDestino\":1")
                        .replace("\"cantidadVehiculos\":4", "\"cantidadVehiculos\":0")))
                .andExpect(status().isNoContent());
        verify(repository).rebalancear(new RebalanceoRequestDTO(1L, 1L, 3L, 0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"P0001", "23503", "40001", "40P01", "42883"})
    void erroresSqlNoReconocidosSonInternosSinFiltrarDetalles(String state) throws Exception {
        doThrow(new UncategorizedSQLException("CALL", "CALL", new SQLException("detalle SQL privado", state)))
                .when(repository).rebalancear(any());
        mvc.perform(post("/api/rebalanceos").contentType("application/json").content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("No fue posible completar el rebalanceo."));
    }

    @Test
    void conexionFallidaEsErrorInterno() throws Exception {
        doThrow(new DataAccessResourceFailureException("credenciales privadas")).when(repository).rebalancear(any());
        mvc.perform(post("/api/rebalanceos").contentType("application/json").content(BODY))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"));
    }
}
