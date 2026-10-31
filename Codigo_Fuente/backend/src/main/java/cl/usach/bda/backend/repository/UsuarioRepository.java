package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.dto.UsuarioAutenticacionDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbcTemplate;

    public UsuarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UsuarioAutenticacionDTO> buscarPorEmail(String email) {
        String sql = """
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
                WHERE LOWER(u.email) = LOWER(?)
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new UsuarioAutenticacionDTO(
                        rs.getLong("id_usuario"),
                        rs.getString("nombre_usuario"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getBoolean("usuario_activo"),
                        rs.getString("nombre_rol")
                ),
                email
        ).stream().findFirst();
    }
}
