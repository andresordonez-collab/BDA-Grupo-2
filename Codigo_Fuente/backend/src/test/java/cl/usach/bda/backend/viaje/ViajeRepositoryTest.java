package cl.usach.bda.backend.viaje;

import cl.usach.bda.backend.repository.ViajeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ViajeRepositoryTest {
    @Test
    void callUsaBigintsEnOrdenYCierraRecursos() throws Exception {
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(source.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("CALL finalizar_viaje(?, ?)" )).thenReturn(statement);
        new ViajeRepository(new JdbcTemplate(source)).finalizar(3_000_000_001L, 3_000_000_002L);
        verify(statement).setLong(1, 3_000_000_001L);
        verify(statement).setLong(2, 3_000_000_002L);
        verify(statement).execute();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void consultasBloqueanFilasSinFiltrarPorPropietario() throws Exception {
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement station = mock(PreparedStatement.class);
        PreparedStatement trip = mock(PreparedStatement.class);
        ResultSet stations = mock(ResultSet.class);
        ResultSet trips = mock(ResultSet.class);
        when(source.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT id_estacion FROM estaciones WHERE id_estacion = ? FOR UPDATE"))
                .thenReturn(station);
        when(connection.prepareStatement("SELECT id_usuario FROM viajes WHERE id_viaje = ? FOR UPDATE"))
                .thenReturn(trip);
        when(station.executeQuery()).thenReturn(stations);
        when(trip.executeQuery()).thenReturn(trips);
        when(stations.next()).thenReturn(true, false);
        when(stations.getLong("id_estacion")).thenReturn(2L);
        when(trips.next()).thenReturn(true, false);
        when(trips.getLong("id_usuario")).thenReturn(7L);
        var repository = new ViajeRepository(new JdbcTemplate(source));
        assertTrue(repository.bloquearEstacion(2L));
        assertEquals(7L, repository.bloquearYObtenerPropietario(1L).orElseThrow());
        verify(station).setObject(1, 2L);
        verify(trip).setObject(1, 1L);
        verify(stations).close();
        verify(trips).close();
    }
}
