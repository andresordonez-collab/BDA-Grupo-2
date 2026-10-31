package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.entities.RolEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RolRepository {

    private final JdbcTemplate jdbcTemplate;

    public RolRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Row mapper para mapear las filas de la tabla a la entidad.
    private static final RowMapper<RolEntity> ROLES_ROW_MAPPER = (rs, rowNum) ->
            RolEntity.builder()
                    .idRol(rs.getLong("id_rol"))
                    .nombreRol(rs.getString("nombre_rol"))
                    .build();

    // Crear un registro y obtener el ID generado por PostgreSQL.
    public RolEntity save(RolEntity rol) {
        String sql = "INSERT INTO roles (nombre_rol)"
                + " VALUES (?) RETURNING id_rol";
        Long idGenerado = jdbcTemplate.queryForObject(sql, Long.class,
                rol.getNombreRol()
        );
        rol.setIdRol(idGenerado);
        return rol;
    }

    // Obtener todos los registros.
    public List<RolEntity> findAll() {
        String sql = "SELECT * FROM roles ORDER BY id_rol ASC";
        return jdbcTemplate.query(sql, ROLES_ROW_MAPPER);
    }

    // Obtener un registro por su ID.
    public Optional<RolEntity> findById(Long idRol) {
        String sql = "SELECT * FROM roles WHERE id_rol = ?";
        return jdbcTemplate.query(sql, ROLES_ROW_MAPPER, idRol)
                .stream()
                .findFirst();
    }

    // Actualizar un registro existente.
    public boolean update(Long idRol, RolEntity rol) {
        String sql = "UPDATE roles SET nombre_rol = ?"
                + " WHERE id_rol = ?";
        int filasAfectadas = jdbcTemplate.update(sql,
                rol.getNombreRol(),
                idRol
        );
        return filasAfectadas > 0;
    }

    // Eliminar un registro por su ID.
    public boolean deleteById(Long idRol) {
        String sql = "DELETE FROM roles WHERE id_rol = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idRol);
        return filasAfectadas > 0;
    }
}
