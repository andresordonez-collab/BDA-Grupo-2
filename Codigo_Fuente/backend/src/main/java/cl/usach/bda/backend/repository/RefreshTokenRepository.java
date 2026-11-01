package cl.usach.bda.backend.repository;

import cl.usach.bda.backend.entities.RefreshTokenEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class RefreshTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public RefreshTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<RefreshTokenEntity> REFRESH_TOKENS_ROW_MAPPER = (rs, rowNum) ->
            RefreshTokenEntity.builder()
                    .idRefreshToken(rs.getLong("id_refresh_token"))
                    .idUsuario(rs.getLong("id_usuario"))
                    .tokenHash(rs.getString("token_hash"))
                    .expiraEn(rs.getObject("expira_en", LocalDateTime.class))
                    .revocado(rs.getObject("revocado", Boolean.class))
                    .build();

    public RefreshTokenEntity save(RefreshTokenEntity refreshToken) {
        String sql = "INSERT INTO refresh_tokens (id_usuario, token_hash, expira_en, revocado)"
                + " VALUES (?, ?, ?, COALESCE(?, FALSE)) RETURNING id_refresh_token";
        Long idGenerado = jdbcTemplate.queryForObject(sql, Long.class,
                refreshToken.getIdUsuario(),
                refreshToken.getTokenHash(),
                refreshToken.getExpiraEn(),
                refreshToken.getRevocado()
        );
        refreshToken.setIdRefreshToken(idGenerado);
        return findById(idGenerado).orElseThrow();
    }

    public List<RefreshTokenEntity> findAll() {
        String sql = "SELECT * FROM refresh_tokens ORDER BY id_refresh_token ASC";
        return jdbcTemplate.query(sql, REFRESH_TOKENS_ROW_MAPPER);
    }

    public Optional<RefreshTokenEntity> findById(Long idRefreshToken) {
        String sql = "SELECT * FROM refresh_tokens WHERE id_refresh_token = ?";
        return jdbcTemplate.query(sql, REFRESH_TOKENS_ROW_MAPPER, idRefreshToken)
                .stream()
                .findFirst();
    }

    public boolean update(Long idRefreshToken, RefreshTokenEntity refreshToken) {
        String sql = "UPDATE refresh_tokens SET id_usuario = ?, token_hash = ?, expira_en = ?, revocado = COALESCE(?, revocado)"
                + " WHERE id_refresh_token = ?";
        int filasAfectadas = jdbcTemplate.update(sql,
                refreshToken.getIdUsuario(),
                refreshToken.getTokenHash(),
                refreshToken.getExpiraEn(),
                refreshToken.getRevocado(),
                idRefreshToken
        );
        return filasAfectadas > 0;
    }

    public boolean deleteById(Long idRefreshToken) {
        String sql = "DELETE FROM refresh_tokens WHERE id_refresh_token = ?";
        int filasAfectadas = jdbcTemplate.update(sql, idRefreshToken);
        return filasAfectadas > 0;
    }
}
