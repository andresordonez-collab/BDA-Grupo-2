package cl.usach.bda.backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PruebaController {

    private final JdbcTemplate jdbcTemplate;

    public PruebaController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/prueba-bd")
    public Map<String, Object> probarConexion() {
        Integer cantidad = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vehiculos",
                Integer.class
        );

        return Map.of(
                "conexion", "correcta",
                "vehiculos", cantidad
        );
    }
}