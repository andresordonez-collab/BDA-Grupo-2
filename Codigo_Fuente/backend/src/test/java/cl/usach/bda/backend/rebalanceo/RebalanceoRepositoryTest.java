package cl.usach.bda.backend.rebalanceo;

import cl.usach.bda.backend.dto.RebalanceoRequestDTO;
import cl.usach.bda.backend.repository.RebalanceoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.mockito.Mockito.*;

class RebalanceoRepositoryTest {
    @Test
    void ejecutaCallConParametrosTipadosEnOrdenYCierraRecursos() throws Exception {
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(source.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("CALL rebalancear_flota(?, ?, ?, ?)"))
                .thenReturn(statement);

        new RebalanceoRepository(new JdbcTemplate(source)).rebalancear(
                new RebalanceoRequestDTO(3_000_000_001L, 3_000_000_002L, 3_000_000_003L, 4));

        verify(statement).setLong(1, 3_000_000_001L);
        verify(statement).setLong(2, 3_000_000_002L);
        verify(statement).setLong(3, 3_000_000_003L);
        verify(statement).setInt(4, 4);
        verify(statement).execute();
        verify(statement).close();
        verify(connection).close();
    }
}
