package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.dto.UsuarioAutenticacionDTO;
import cl.usach.bda.backend.entities.UsuarioEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbcTemplate;

    public UsuarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Row mapper para mapear las filas de la tabla a la entidad.

    private static final RowMapper<UsuarioEntity> USUARIO_ROW_MAPPER = (rs, rowNum) ->
            UsuarioEntity.builder()
                    .idUsuario(rs.getLong("id_usuario"))
                    .idRol(rs.getLong("id_rol"))
                    .nombreUsuario(rs.getString("nombre_usuario"))
                    .email(rs.getString("email"))
                    .passwordHash(rs.getString("password_hash"))
                    .usuarioActivo(rs.getObject("usuario_activo", Boolean.class))
                    .fechaRegistro(rs.getObject("fecha_registro", LocalDateTime.class))
                    .build();

    // CRUD.

    // CREATE

    public UsuarioEntity save(UsuarioEntity usuario) {
        String sql = """
                INSERT INTO usuarios (id_rol, nombre_usuario, email, password_hash, usuario_activo, fecha_registro)
                VALUES (?, ?, ?, ?, COALESCE(?,TRUE), COALESCE(?, LOCALTIMESTAMP))
                RETURNING id_usuario
                """;
        Long idGenerado = jdbcTemplate.queryForObject(sql, Long.class,
                usuario.getIdRol(),
                usuario.getNombreUsuario(),
                usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getUsuarioActivo(),
                usuario.getFechaRegistro()
        );
        return findById(idGenerado)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo recuperar el usuario recién creado."));
    }

    // READ

    // Obtener todos los registros de usuarios.
    public List<UsuarioEntity> findAll() {
        String sql = "SELECT * FROM usuarios ORDER BY id_usuario ASC";
        return jdbcTemplate.query(sql, USUARIO_ROW_MAPPER);
    }

    // Obtener un registro por su ID.
    public Optional<UsuarioEntity> findById(Long idUsuario) {
        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";
        return jdbcTemplate.query(sql, USUARIO_ROW_MAPPER, idUsuario)
                .stream()
                .findFirst();
    }

    // Actualizar un registro existente.
    public boolean update(Long idUsuario, UsuarioEntity usuario) {
        String sql = """
        UPDATE usuarios
        SET id_rol = ?,
            nombre_usuario = ?,
            email = ?,
            password_hash = COALESCE(?, password_hash),
            usuario_activo = COALESCE(?, usuario_activo)
        WHERE id_usuario = ?
        """;

        int filasAfectadas = jdbcTemplate.update(sql,
                usuario.getIdRol(),
                usuario.getNombreUsuario(),
                usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getUsuarioActivo(),
                idUsuario
        );
        return filasAfectadas > 0;
    }

    // Eliminar un registro por su ID.
    public boolean deleteById(Long idUsuario) {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idUsuario);
        return filasAfectadas > 0;
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
