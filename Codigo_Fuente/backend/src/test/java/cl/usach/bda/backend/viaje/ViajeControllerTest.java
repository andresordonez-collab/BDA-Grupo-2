package cl.usach.bda.backend.viaje;

import cl.usach.bda.backend.controller.ViajeController;
import cl.usach.bda.backend.exception.GlobalExceptionHandler;
import cl.usach.bda.backend.repository.ViajeRepository;
import cl.usach.bda.backend.security.JwtService;
import cl.usach.bda.backend.security.SecurityConfig;
import cl.usach.bda.backend.service.ViajeService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ViajeController.class)
@Import({SecurityConfig.class, JwtService.class, ViajeService.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "jwt.secret=clave-de-prueba-de-al-menos-32-bytes-para-hs256", "jwt.expiration=60000"})
class ViajeControllerTest {
    private static final String URL = "/api/viajes/1/finalizar";
    private static final String BODY = "{\"idEstacionDestino\":2}";
    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @MockitoBean ViajeRepository repository;

    private Cookie cookie(String rol) {
        return new Cookie("access_token", jwtService.generarToken(7L, rol));
    }

    @BeforeEach
    void setup() {
        when(repository.bloquearEstacion(2L)).thenReturn(true);
        when(repository.bloquearYObtenerPropietario(1L)).thenReturn(Optional.of(7L));
    }

    @Test
    void sinAutenticacionResponde401() throws Exception {
        mvc.perform(post(URL).contentType("application/json").content(BODY)).andExpect(status().isUnauthorized());
        verifyNoInteractions(repository);
    }

    @Test
    void cookieInvalidaResponde401() throws Exception {
        mvc.perform(post(URL).cookie(new Cookie("access_token", "invalido"))
                        .contentType("application/json").content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(repository);
    }

    @Test
    void operadorNoPuedeFinalizar() throws Exception {
        mvc.perform(post(URL).cookie(cookie("Operador")).contentType("application/json").content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(repository);
    }

    @Test
    void propietarioFinalizaCon204SinCuerpo() throws Exception {
        mvc.perform(post(URL).cookie(cookie("Usuario")).contentType("application/json").content(BODY))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(repository).finalizar(1L, 2L);
    }

    @Test
    void viajeAjenoNoEjecutaProcedimiento() throws Exception {
        when(repository.bloquearYObtenerPropietario(1L)).thenReturn(Optional.of(8L));
        mvc.perform(post(URL).cookie(cookie("Usuario")).contentType("application/json").content(BODY))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.mensaje").value("Viaje no encontrado."));
        verify(repository, never()).finalizar(any(), any());
    }

    @Test
    void identidadEnJsonNoPermiteSuplantarAlPropietario() throws Exception {
        when(repository.bloquearYObtenerPropietario(1L)).thenReturn(Optional.of(8L));
        mvc.perform(post(URL).cookie(cookie("Usuario")).contentType("application/json")
                        .content("{\"idEstacionDestino\":2,\"idUsuario\":8}"))
                .andExpect(status().isNotFound());
        verify(repository, never()).finalizar(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{", "{}", "null", "{\"idEstacionDestino\":null}",
            "{\"idEstacionDestino\":0}", "{\"idEstacionDestino\":-1}",
            "{\"idEstacionDestino\":\"2\"}", "{\"idEstacionDestino\":1.5}",
            "{\"idEstacionDestino\":true}", "{\"idEstacionDestino\":9223372036854775808}"})
    void entradaInvalidaNoConsultaBase(String body) throws Exception {
        mvc.perform(post(URL).cookie(cookie("Usuario")).contentType("application/json").content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc", "9223372036854775808"})
    void rutaInvalidaNoConsultaBase(String id) throws Exception {
        mvc.perform(post("/api/viajes/" + id + "/finalizar").cookie(cookie("Usuario"))
                        .contentType("application/json").content(BODY))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void rechazoConocidoResponde409SinDetallesSql() throws Exception {
        doThrow(new UncategorizedSQLException("CALL", "privado", new SQLException(
                "ERROR: Error: La estación de destino 2 está llena. CONTEXT privado", "P0001")))
                .when(repository).finalizar(1L, 2L);
        mvc.perform(post(URL).cookie(cookie("Usuario")).contentType("application/json").content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("FINALIZAR_VIAJE_RECHAZADO"))
                .andExpect(jsonPath("$.mensaje").value("El viaje no puede finalizarse en la estación indicada."));
    }

    @Test
    void integridadNoExponeDetalleSql() throws Exception {
        doThrow(new DataIntegrityViolationException("detalle SQL privado"))
                .when(repository).finalizar(1L, 2L);
        mvc.perform(post(URL).cookie(cookie("Usuario")).contentType("application/json").content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("No fue posible completar la solicitud."));
    }

    @Test
    void p0001DesconocidoEsInterno() throws Exception {
        doThrow(new UncategorizedSQLException("CALL", "privado", new SQLException("detalle privado", "P0001")))
                .when(repository).finalizar(1L, 2L);
        mvc.perform(post(URL).cookie(cookie("Usuario")).contentType("application/json").content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.mensaje").value("No fue posible completar la solicitud."));
    }
}
