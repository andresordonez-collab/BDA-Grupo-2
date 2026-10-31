package cl.usach.bda.backend.viaje;

import cl.usach.bda.backend.exception.FinalizarViajeNegocioException;
import cl.usach.bda.backend.repository.ViajeRepository;
import cl.usach.bda.backend.service.ViajeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ViajeServiceTest {
    private ViajeRepository repository;
    private ViajeService service;

    @BeforeEach
    void setup() {
        repository = mock(ViajeRepository.class);
        service = new ViajeService(repository);
    }

    @Test
    void bloqueaEstacionAntesDelViajeYFinalizaSoloSiEsPropio() {
        when(repository.bloquearEstacion(2L)).thenReturn(true);
        when(repository.bloquearYObtenerPropietario(1L)).thenReturn(Optional.of(7L));
        service.finalizar(1L, 2L, 7L);
        var order = inOrder(repository);
        order.verify(repository).bloquearEstacion(2L);
        order.verify(repository).bloquearYObtenerPropietario(1L);
        order.verify(repository).finalizar(1L, 2L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void viajeAjenoONoExistenteNoEjecutaCall() {
        when(repository.bloquearEstacion(2L)).thenReturn(true);
        for (Optional<Long> owner : java.util.List.of(Optional.of(8L), Optional.<Long>empty())) {
            when(repository.bloquearYObtenerPropietario(1L)).thenReturn(owner);
            assertEquals("Viaje no encontrado.", assertThrows(NoSuchElementException.class,
                    () -> service.finalizar(1L, 2L, 7L)).getMessage());
        }
        verify(repository, never()).finalizar(any(), any());
    }

    @Test
    void estacionInexistenteNoBloqueaViaje() {
        assertThrows(FinalizarViajeNegocioException.class, () -> service.finalizar(1L, 2L, 7L));
        verify(repository).bloquearEstacion(2L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void sinIdentidadOIdsInvalidosNoConsultaBase() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> service.finalizar(1L, 2L, null));
        assertThrows(IllegalArgumentException.class, () -> service.finalizar(0L, 2L, 7L));
        assertThrows(IllegalArgumentException.class, () -> service.finalizar(1L, -2L, 7L));
        verifyNoInteractions(repository);
    }

    @Test
    void traduceSoloRechazosConocidosSinExponerSql() {
        when(repository.bloquearEstacion(2L)).thenReturn(true);
        when(repository.bloquearYObtenerPropietario(1L)).thenReturn(Optional.of(7L));
        doThrow(new UncategorizedSQLException("CALL", "SQL privado",
                new SQLException("ERROR: Error: El viaje 1 no existe o ya está Finalizado.", "P0001")))
                .when(repository).finalizar(1L, 2L);
        assertEquals("El viaje no puede finalizarse en la estación indicada.",
                assertThrows(FinalizarViajeNegocioException.class, () -> service.finalizar(1L, 2L, 7L)).getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"P0001", "23503", "40001", "40P01", "42883"})
    void erroresDesconocidosNoSeClasificanComoNegocio(String state) {
        var error = new UncategorizedSQLException("CALL", "privado", new SQLException("privado", state));
        when(repository.bloquearEstacion(2L)).thenThrow(error);
        assertSame(error, assertThrows(UncategorizedSQLException.class, () -> service.finalizar(1L, 2L, 7L)));
    }
}
