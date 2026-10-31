package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.dto.UsuarioAutenticacionDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsuarioRepository {

    private static final String SQL_BASE = """
            SELECT
                u.id_usuario,
                u.nombre_usuario,
                u.email,
                u.password_hash,
                u.usuario_activo,
                r.nombre_rol
            FROM usuarios u
            JOIN roles r
                ON u.id_rol = r.id_rol
            """;

    private static final RowMapper<UsuarioAutenticacionDTO> MAPPER = (rs, rowNum) ->
            new UsuarioAutenticacionDTO(
                    rs.getLong("id_usuario"),
                    rs.getString("nombre_usuario"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getBoolean("usuario_activo"),
                    rs.getString("nombre_rol")
            );

    private final JdbcTemplate jdbcTemplate;

    public UsuarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UsuarioAutenticacionDTO> buscarPorEmail(String email) {
        return jdbcTemplate.query(SQL_BASE + "WHERE LOWER(u.email) = LOWER(?)", MAPPER, email)
                .stream().findFirst();
    }

    public Optional<UsuarioAutenticacionDTO> buscarPorId(Long idUsuario) {
        return jdbcTemplate.query(SQL_BASE + "WHERE u.id_usuario = ?", MAPPER, idUsuario)
                .stream().findFirst();
    }
}